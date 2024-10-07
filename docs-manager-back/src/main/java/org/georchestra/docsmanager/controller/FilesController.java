package org.georchestra.docsmanager.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Collections;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import jakarta.servlet.http.HttpServletRequest;
import org.georchestra.docsmanager.helper.FileEntityHelper;
import org.georchestra.docsmanager.helper.RoleHelper;
import org.georchestra.docsmanager.model.FileEntity;
import org.georchestra.docsmanager.model.FileResponse;
import org.georchestra.docsmanager.service.FileService;
import org.slf4j.Logger;

@RestController
@RequestMapping("files")
public class FilesController {
        final Logger logger = LoggerFactory.getLogger(FilesController.class);

        private final FileService fileService;

        private final String HEADER_ROLE = "sec-roles";
        private final String HEADER_USERNAME = "sec-username";
        private final String HEADER_ORG = "sec-orgname";

        @Value("${docs.roles.additionnal:{}}")
        String additionalRoles;
        @Value("${docs.roles.admin}")
        List<String> adminRoles;
        @Value("${docs.public.value}")
        String publicValue;
        @Value("${docs.context.path:}")
        String contextPath;

        @Autowired
        public FilesController(FileService fileService) {
                this.fileService = fileService;
        }

        /**
         * [protected] - Upload a file
         * 
         * @param request
         * @param file - blob
         * @param comment
         * @param label
         * @param dateDoc
         * @param plugin - code or id plugin
         * @param status - e.g public
         * @param entity - id feature
         * @return
         */
        @PostMapping(value = "/plugin/{plugin}")
        public ResponseEntity<String> upload(HttpServletRequest request,
                        @RequestParam("file") MultipartFile file,
                        @RequestParam(value = "comment", required = false) String comment,
                        @RequestParam("label") String label,
                        @RequestParam("dateDoc") String dateDoc,
                        @RequestParam(value = "status", required = false) String status,
                        @RequestParam("entity") String entity,
                        @RequestParam(value = "opened", defaultValue = "false",
                                        required = false) Boolean opened,
                        @PathVariable String plugin) {
                try {
                        String roles = request.getHeader(HEADER_ROLE);
                        String username = request.getHeader(HEADER_USERNAME);

                        List<String> defaultWriteRoles = RoleHelper.getFullAuthorizedRoles(plugin,
                                        "edit", adminRoles, additionalRoles);

                        if (!RoleHelper.isWriter(plugin, roles, defaultWriteRoles)) {
                                logger.info("Upload failed : check user ROLES (header sec-roles)");
                                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(String
                                                .format("Not authorized to upload the file: %s",
                                                                file.getOriginalFilename()));
                        }
                        fileService.save(file, plugin, comment, username, label, dateDoc, status,
                                        entity, opened);
                        return ResponseEntity.status(HttpStatus.OK)
                                        .body(String.format("File uploaded successfully: %s",
                                                        file.getOriginalFilename()));
                } catch (Exception e) {
                        logger.error("Upload failed due to Unknown error", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(String.format("Could not upload the file: %s!",
                                                        file.getOriginalFilename()));
                }
        }

        /**
         * 
         * @param request
         * @param comment
         * @param label
         * @param dateDoc
         * @param status
         * @param entity - id feature
         * @param opened
         * @param id - file
         * @param plugin
         * @return
         */
        @PutMapping("/plugin/{plugin}/{id}")
        public ResponseEntity<String> update(HttpServletRequest request,
                        @RequestBody FileEntity fileToUp,
                        @PathVariable String id,
                        @PathVariable String plugin
                ) {
                try {
                        String roles = request.getHeader(HEADER_ROLE);

                        List<String> defaultWriteRoles = RoleHelper.getFullAuthorizedRoles(plugin,
                                        "edit", adminRoles, additionalRoles);

                        if (!RoleHelper.isWriter(plugin, roles, defaultWriteRoles)) {
                                logger.info("Upload failed : check user ROLES (header sec-roles)");
                                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(String
                                                .format("Not authorized to upload the file with id: %s",
                                                                id));
                        }
                        // chek if exists
                        Optional<FileEntity> optionalFileToUp = fileService.getFile(id);
                        if (!optionalFileToUp.isPresent()) {
                                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                                                String.format("File with id %s not found !", id));
                        }

                        fileService.update(fileToUp);

                        return ResponseEntity.status(HttpStatus.OK)
                                        .body(String.format("File successfully updated: %s", id));
                } catch (Exception e) {
                        logger.error("Upload failed due to Unknown error", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String
                                        .format("Could not update the file with id : %s!", id));
                }
        }


        /**
         * [protected] - List all files
         * 
         * @param request
         * @param plugin - code or id plugin
         * @param status - e.g public
         * @param entity - id feature
         * @param label
         * @return
         */
        @GetMapping("/all")
        public List<FileResponse> list(HttpServletRequest request,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) String entity,
                        @RequestParam(required = false) String plugin,
                        @RequestParam(required = false) String label) {
                String roles = request.getHeader(HEADER_ROLE);
                List<FileEntity> responseFiles;
                FileEntity searchFile =
                                FileEntityHelper.getFileExample(status, plugin, entity, label);
                if (!RoleHelper.isAdmin(roles, adminRoles)) {
                        logger.info("GET /all : Not autorized roles [%s]".formatted(roles));
                        return Collections.emptyList();
                }
                responseFiles = fileService.getAllFilesFromExample(searchFile);
                return responseFiles.stream().map(this::mapToFileResponse)
                                .collect(Collectors.toList());
        }

        /**
         * [protected] - Get all files by plugin value
         * 
         * @param request
         * @param plugin - code or id plugin
         * @param status - e.g public
         * @param entity - id feature
         * @param label
         * @return
         */
        @GetMapping(value = "/plugin/{plugin}")
        public List<FileResponse> listFilesByPlugin(HttpServletRequest request,
                        @PathVariable String plugin, @RequestParam(required = false) String status,
                        @RequestParam(required = false) String entity,
                        @RequestParam(required = false) String label) {
                String roles = request.getHeader(HEADER_ROLE);
                List<String> defaultReadersRoles = RoleHelper.getFullAuthorizedRoles(plugin, "read",
                                adminRoles, additionalRoles);

                List<FileEntity> responseFiles;
                FileEntity searchFile =
                                FileEntityHelper.getFileExample(status, plugin, entity, label);

                Boolean onlyReadOpenFiles = roles == null
                                || !RoleHelper.isReader(plugin, roles, defaultReadersRoles);
                if (onlyReadOpenFiles) {
                        searchFile.setOpened(onlyReadOpenFiles);
                } else {
                        searchFile.setOpened(null);
                }

                responseFiles = fileService.getAllFilesFromExample(searchFile);
                return responseFiles.stream().map(this::mapToFileResponse)
                                .collect(Collectors.toList());

        }


        /**
         * Utility func to create a file response.
         * 
         * @param fileEntity
         * @return
         */
        private FileResponse mapToFileResponse(FileEntity fileEntity) {
                String downloadURL = "/plugin/" + fileEntity.getPlugin() + "/" + fileEntity.getId();

                if (contextPath != null) {
                        downloadURL = contextPath + downloadURL;
                } else {
                        downloadURL = "files" + downloadURL;
                }
                FileResponse fileResponse = new FileResponse();
                fileResponse.setId(fileEntity.getId());
                fileResponse.setName(fileEntity.getName());
                fileResponse.setContentType(fileEntity.getContentType());
                fileResponse.setSize(fileEntity.getSize());
                fileResponse.setUrl(downloadURL);
                fileResponse.setLabel(fileEntity.getLabel());
                fileResponse.setPlugin(fileEntity.getPlugin());
                fileResponse.setDateDoc(fileEntity.getDateDoc());
                fileResponse.setStatus(fileEntity.getStatus());
                fileResponse.setComment(fileEntity.getComment());
                fileResponse.setEntity(fileEntity.getEntity());
                fileResponse.setOpened(fileEntity.getOpened());

                return fileResponse;
        }

        /**
         * Check if a label exist
         * 
         * @param request
         * @param label
         * @return
         */
        @GetMapping("/label/exists/{label}")
        public ResponseEntity<Boolean> labelExists(HttpServletRequest request,
                        @PathVariable String label) {
                Boolean test = fileService.existsByLabel(label);
                return ResponseEntity.status(HttpStatus.OK).body(test);
        }

        /**
         * [protected] - Delete file
         * 
         * @param request
         * @param id - file ID
         * @param plugin - plugin name or code
         * @return
         */
        @DeleteMapping("/plugin/{plugin}/{id}")
        public ResponseEntity<String> deleteFile(HttpServletRequest request,
                        @PathVariable String id, @PathVariable String plugin) {
                String roles = request.getHeader(HEADER_ROLE);

                Boolean fileExists = fileService.existsByIdLike(id);

                List<String> defaultWriters = RoleHelper.getFullAuthorizedRoles(plugin, "edit",
                                adminRoles, additionalRoles);
                Boolean isWriter = RoleHelper.isWriter(plugin, roles, defaultWriters);
                if (!fileExists) {
                        logger.info("DELETE /plugin/{plugin}/{id} : No document exists with this ID -> Check ID.");
                        return ResponseEntity.notFound().build();
                }
                if (!isWriter) {
                        logger.info("DELETE /plugin/{plugin}/{id} : Not autorized roles [%s]"
                                        .formatted(roles));
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(String
                                        .format("You are not authorized to delete this file !"));
                }

                fileService.delete(id);
                return ResponseEntity.status(HttpStatus.OK)
                                .body(String.format("File successfully deleted !"));
        }

        /**
         * [Protected] - Get file by ID
         * 
         * @param request
         * @param id - file ID
         * @param plugin - plugin name or code
         * @return
         */
        @GetMapping("/plugin/{plugin}/{id}")
        public ResponseEntity<byte[]> getFile(HttpServletRequest request, @PathVariable String id,
                        @PathVariable String plugin) {
                String roles = request.getHeader(HEADER_ROLE);

                List<String> defaultReaders = RoleHelper.getFullAuthorizedRoles(plugin, "read",
                                adminRoles, additionalRoles);

                // check if exists
                Boolean idExists = fileService.existsByIdLike(id);
                if (!idExists) {
                        logger.info("GET /plugin/{plugin}/{id} : No document exists with this ID -> Check ID.");
                        return ResponseEntity.notFound().build();
                }

                // read file
                Optional<FileEntity> fileEntityOptional = fileService.getFile(id);

                FileEntity fileEntity = fileEntityOptional.get();

                // fobid if not opened file
                if (!fileEntity.getOpened()
                                && !RoleHelper.isReader(plugin, roles, defaultReaders)) {
                        logger.info("GET /plugin/{plugin}/{id} : Not autorized roles [%s]"
                                        .formatted(roles));
                        return ResponseEntity.notFound().build();
                }

                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=\"" + fileEntity.getName()
                                                                + "\"")
                                .contentType(MediaType.valueOf(fileEntity.getContentType()))
                                .body(fileEntity.getData());
        }
}
