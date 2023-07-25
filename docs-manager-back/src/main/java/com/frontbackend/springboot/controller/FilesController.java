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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import org.springframework.beans.factory.annotation.Value;

import com.frontbackend.springboot.model.FileEntity;
import com.frontbackend.springboot.model.FileResponse;
import com.frontbackend.springboot.service.FileService;
import com.frontbackend.springboot.helper.RoleHelper;
import com.frontbackend.springboot.helper.UserHelper;

import org.slf4j.Logger;

@RestController
@RequestMapping("files")
public class FilesController {
    final Logger logger = LoggerFactory.getLogger(FilesController.class);

    private final FileService fileService;

    private final String HEADER_ROLE = "sec-roles";
    private final String HEADER_USERNAME = "sec-username";
    private final String HEADER_ORG = "sec-orgname";

    @Value("${docs.roles.additionnal}")
    String additionalRoles;
    @Value("${docs.roles.admin}")
    List<String> adminRoles;

    @Autowired
    public FilesController(FileService fileService) {
        this.fileService = fileService;
    }
    @PostMapping(value = "/plugin/{plugin}")
    public ResponseEntity<String> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("comment") String comment,
            @RequestParam("label") String label,
            @RequestHeader(HEADER_ROLE) String roles,
            @RequestHeader(HEADER_USERNAME) String username,
            @RequestHeader(HEADER_ORG) String org,
            @PathVariable String plugin) {
        try {
            List<String> defaultWriteRoles = RoleHelper.getFullAuthorizedRoles(plugin, "edit", adminRoles, additionalRoles);
            if (RoleHelper.isWriter(plugin, roles, defaultWriteRoles)) {
                String userInfos = UserHelper.UserInfosAsJson(roles, org, username);
                fileService.save(file, plugin, comment, userInfos, label);
                return ResponseEntity.status(HttpStatus.OK)
                        .body(String.format("File uploaded successfully: %s", file.getOriginalFilename()));
            } else {
                logger.info("Upload failed : check user ROLES (header sec-roles)");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(String.format("Not authorized to upload the file: %s", file.getOriginalFilename()));
            }
        } catch (Exception e) {
            logger.error("Upload failed due to Unknown error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(String.format("Could not upload the file: %s!", file.getOriginalFilename()));
        }
    }

    @PatchMapping("/plugin/{plugin}/{id}")
    public ResponseEntity<String> updateLabel(
        @RequestHeader(HEADER_ROLE) String role,
        @PathVariable String plugin
    ) {
        List<String> defaultWriteRoles = RoleHelper.getFullAuthorizedRoles(plugin, "edit", adminRoles, additionalRoles);
        if (!RoleHelper.isReader(plugin, role, defaultWriteRoles)) {
            logger.debug(
                    "PATCH /plugin/{plugin}/{id} : Not authorized to update label -> Check ROLES.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(String.format("You are not authorized to delete this file !"));
        }
        return ResponseEntity.status(HttpStatus.OK)
            .body(String.format("Update label : Success !"));
    }

    @GetMapping("/all")
    public List<FileResponse> list(
            @RequestHeader(HEADER_ROLE) String role,
            @Value("${docs.roles.admin}") List<String> adminRoles) {
        if (RoleHelper.isAdmin(role, adminRoles)) {
            return fileService.getAllFiles()
                    .stream()
                    .map(this::mapToFileResponse)
                    .collect(Collectors.toList());
        } else {
            logger.debug(
                    "GET /all : return empty list -> Only admin can read all documents ! Change properties config file if needed.");
            return Collections.emptyList();
        }
    }

    @GetMapping(value = "/plugin/{plugin}")
    public List<FileResponse> listByPlugin(
            @RequestHeader(HEADER_ROLE) String role,
            @PathVariable String plugin) {

        List<String> defaultReadersRoles = RoleHelper.getFullAuthorizedRoles(plugin, "read", adminRoles, additionalRoles);
        if (RoleHelper.isReader(plugin, role, defaultReadersRoles)) {
            return fileService.findByPlugin(plugin.toUpperCase())
                    .stream()
                    .map(this::mapToFileResponse)
                    .collect(Collectors.toList());
        } else {
            logger.debug(
                    "GET /plugin/{plugin} : return empty list -> Only plugin readers are allowed ! Check user ROLES.");
            return Collections.emptyList();
        }

    }

    private FileResponse mapToFileResponse(FileEntity fileEntity) {
        String downloadURL = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/files/")
                .path(fileEntity.getId())
                .toUriString();
        FileResponse fileResponse = new FileResponse();
        fileResponse.setId(fileEntity.getId());
        fileResponse.setName(fileEntity.getName());
        fileResponse.setContentType(fileEntity.getContentType());
        fileResponse.setSize(fileEntity.getSize());
        fileResponse.setUrl(downloadURL);
        fileResponse.setLabel(fileEntity.getLabel());

        return fileResponse;
    }

    @DeleteMapping("/plugin/{plugin}/{id}")
    public ResponseEntity<String> deleteFile(
            @RequestHeader(HEADER_ROLE) String role,
            @PathVariable String id,
            @PathVariable String plugin) {
        Optional<FileEntity> fileEntityOptional = fileService.getFile(id);

        List<String> defaultWriters = RoleHelper.getFullAuthorizedRoles(plugin, "edit", adminRoles, additionalRoles);
        Boolean isWriter = RoleHelper.isWriter(plugin, role, defaultWriters);
        if (!fileEntityOptional.isPresent()) {
            logger.debug(
                    "DELETE /plugin/{plugin}/{id} : No document exists with this ID -> Check ID.");
            return ResponseEntity.notFound()
                    .build();
        }
        if (!isWriter) {
            logger.debug(
                    "DELETE /plugin/{plugin}/{id} : Only writers are allowed to delete -> Check user ROLE");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(String.format("You are not authorized to delete this file !"));
        }

        fileService.delete(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(String.format("File successfully deleted !"));
    }

    @GetMapping("/plugin/{plugin}/{id}")
    public ResponseEntity<byte[]> getFile(
        @PathVariable String id,
        @PathVariable String plugin,
        @RequestHeader(HEADER_ROLE) String role
        ) {
        Optional<FileEntity> fileEntityOptional = fileService.getFile(id);

        if (!fileEntityOptional.isPresent()) {
            logger.debug(
                    "GET /plugin/{plugin}/{id} : No document exists with this ID -> Check ID.");
            return ResponseEntity.notFound()
                    .build();
        }
        List<String> defaultReaders = RoleHelper.getFullAuthorizedRoles(plugin, "edit", adminRoles, additionalRoles);
        if (!RoleHelper.isReader(plugin, role, defaultReaders)) {
            logger.debug(
                    "GET /plugin/{plugin}/{id} : Not authorized to read -> Check ROLES.");
            return ResponseEntity.notFound()
                    .build();
        }

        FileEntity fileEntity = fileEntityOptional.get();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileEntity.getName() + "\"")
                .contentType(MediaType.valueOf(fileEntity.getContentType()))
                .body(fileEntity.getData());
    }

}
