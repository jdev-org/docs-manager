package com.frontbackend.springboot.controller;

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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import org.springframework.beans.factory.annotation.Value;

import com.frontbackend.springboot.model.FileEntity;
import com.frontbackend.springboot.model.FileResponse;
import com.frontbackend.springboot.service.FileService;

import jakarta.servlet.http.HttpServletRequest;

import com.frontbackend.springboot.helper.RoleHelper;
import com.frontbackend.springboot.helper.UserHelper;
import com.frontbackend.springboot.helper.FileEntityHelper;

import org.slf4j.Logger;

@CrossOrigin(origins = "http://localhost:8082")
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

        @Autowired
        public FilesController(FileService fileService) {
                this.fileService = fileService;
        }

        @PostMapping(value = "/plugin/{plugin}")
        public ResponseEntity<String> upload(HttpServletRequest request,
                        @RequestParam("file") MultipartFile file, @RequestParam("comment") String comment,
                        @RequestParam("label") String label, @RequestParam("dateDoc") String dateDoc,
                        @RequestParam("status") String status, @PathVariable String plugin) {
                try {
                        String roles = request.getHeader(HEADER_ROLE);
                        String username = request.getHeader(HEADER_USERNAME);
                        String org = request.getHeader(HEADER_ORG);

                        List<String> defaultWriteRoles = RoleHelper.getFullAuthorizedRoles(plugin, "edit", adminRoles,
                                        additionalRoles);

                        if (!RoleHelper.isWriter(plugin, roles, defaultWriteRoles)) {
                                logger.info("Upload failed : check user ROLES (header sec-roles)");
                                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(String.format(
                                                "Not authorized to upload the file: %s", file.getOriginalFilename()));
                        }
                        String userInfos = UserHelper.UserInfosAsJson(roles, org, username);
                        fileService.save(file, plugin, comment, userInfos, label, dateDoc, status);
                        return ResponseEntity.status(HttpStatus.OK).body(
                                        String.format("File uploaded successfully: %s", file.getOriginalFilename()));
                } catch (Exception e) {
                        logger.error("Upload failed due to Unknown error");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                        String.format("Could not upload the file: %s!", file.getOriginalFilename()));
                }
        }

        @PatchMapping("/plugin/{plugin}/{id}")
        public ResponseEntity<String> updateLabel(HttpServletRequest request,
                        @PathVariable String plugin) {
                String roles = request.getHeader(HEADER_ROLE);

                List<String> defaultWriteRoles = RoleHelper.getFullAuthorizedRoles(plugin, "edit", adminRoles,
                                additionalRoles);
                if (!RoleHelper.isReader(plugin, roles, defaultWriteRoles)) {
                        logger.info("PATCH /plugin/{plugin}/{id} : Not autorized roles [%s]".formatted(roles));
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(String.format("You are not authorized to delete this file !"));
                }
                return ResponseEntity.status(HttpStatus.OK).body(String.format("Update label : Success !"));
        }

        @GetMapping("/all")
        public List<FileResponse> list(HttpServletRequest request,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) String entity,
                        @RequestParam(required = false) String plugin,
                        @RequestParam(required = false) String label) {
                String roles = request.getHeader(HEADER_ROLE);
                List<FileEntity> responseFiles;
                FileEntity searchFile = FileEntityHelper.getFileExample(status, plugin, entity, label);
                if (!RoleHelper.isAdmin(roles, adminRoles)) {
                        logger.info("GET /all : Not autorized roles [%s]".formatted(roles));
                        return Collections.emptyList();
                }
                responseFiles = fileService.getAllFilesFromExample(searchFile);
                return responseFiles.stream().map(this::mapToFileResponse).collect(Collectors.toList());
        }

        @GetMapping(value = "/plugin/{plugin}")
        public List<FileResponse> listByPlugin(HttpServletRequest request, @PathVariable String plugin,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) String entity,
                        @RequestParam(required = false) String label) {
                String roles = request.getHeader(HEADER_ROLE);
                List<String> defaultReadersRoles = RoleHelper.getFullAuthorizedRoles(plugin, "read", adminRoles,
                                additionalRoles);
                if (!RoleHelper.isReader(plugin, roles, defaultReadersRoles)) {
                        logger.info("GET /plugin/{plugin} : Not autorized roles [%s]".formatted(roles));
                        return Collections.emptyList();
                }
                FileEntity searchFile = FileEntityHelper.getFileExample(status, plugin, entity, label);
                return fileService.getAllFilesFromExample(searchFile).stream().map(this::mapToFileResponse)
                                .collect(Collectors.toList());
        }

        @GetMapping(value = "/plugin/{plugin}/label/{label}")
        public List<FileResponse> listByLabel(HttpServletRequest request, @PathVariable String plugin,
                        @PathVariable String label) {
                String roles = request.getHeader(HEADER_ROLE);
                List<String> defaultReadersRoles = RoleHelper.getFullAuthorizedRoles(plugin, "read", adminRoles,
                                additionalRoles);
                if (!RoleHelper.isReader(plugin, roles, defaultReadersRoles)) {
                        logger.info("GET /plugin/{plugin} : Not autorized roles [%s]".formatted(roles));
                        return Collections.emptyList();
                }
                return fileService.getFileByLabel(label).stream().map(this::mapToFileResponse)
                                .collect(Collectors.toList());
        }

        private FileResponse mapToFileResponse(FileEntity fileEntity) {
                String downloadURL = ServletUriComponentsBuilder.fromCurrentContextPath().path("/files/")
                                .path(fileEntity.getId()).toUriString();
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

                return fileResponse;
        }

        @DeleteMapping("/plugin/{plugin}/{id}")
        public ResponseEntity<String> deleteFile(HttpServletRequest request, @PathVariable String id,
                        @PathVariable String plugin) {
                String roles = request.getHeader(HEADER_ROLE);

                Optional<FileEntity> fileEntityOptional = fileService.getFile(id);

                List<String> defaultWriters = RoleHelper.getFullAuthorizedRoles(plugin, "edit", adminRoles,
                                additionalRoles);
                Boolean isWriter = RoleHelper.isWriter(plugin, roles, defaultWriters);
                if (!fileEntityOptional.isPresent()) {
                        logger.info(
                                        "DELETE /plugin/{plugin}/{id} : No document exists with this ID -> Check ID.");
                        return ResponseEntity.notFound().build();
                }
                if (!isWriter) {
                        logger.info("DELETE /plugin/{plugin}/{id} : Not autorized roles [%s]".formatted(roles));
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(String.format("You are not authorized to delete this file !"));
                }

                fileService.delete(id);
                return ResponseEntity.status(HttpStatus.OK)
                                .body(String.format("File successfully deleted !"));
        }

        @GetMapping("/plugin/{plugin}/{id}")
        public ResponseEntity<byte[]> getFile(HttpServletRequest request, @PathVariable String id,
                        @PathVariable String plugin) {
                String roles = request.getHeader(HEADER_ROLE);

                Optional<FileEntity> fileEntityOptional = fileService.getFile(id);

                if (!fileEntityOptional.isPresent()) {
                        logger.info("GET /plugin/{plugin}/{id} : No document exists with this ID -> Check ID.");
                        return ResponseEntity.notFound().build();
                }
                List<String> defaultReaders = RoleHelper.getFullAuthorizedRoles(plugin, "read", adminRoles,
                                additionalRoles);
                if (!RoleHelper.isReader(plugin, roles, defaultReaders)) {
                        logger.info("GET /plugin/{plugin}/{id} : Not autorized roles [%s]".formatted(roles));
                        return ResponseEntity.notFound().build();
                }

                FileEntity fileEntity = fileEntityOptional.get();
                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=\"" + fileEntity.getName() + "\"")
                                .contentType(MediaType.valueOf(fileEntity.getContentType()))
                                .body(fileEntity.getData());
        }
}
