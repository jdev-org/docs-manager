package com.frontbackend.springboot.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.frontbackend.springboot.model.FileEntity;
import com.frontbackend.springboot.repository.FileRepository;

@Service
public class FileService {

    private final FileRepository fileRepository;

    @Autowired
    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public void save(MultipartFile file, String plugin, String comment, String userInfos, String label)
            throws IOException {
        FileEntity fileEntity = new FileEntity();
        fileEntity.setName(StringUtils.cleanPath(file.getOriginalFilename()));
        fileEntity.setContentType(file.getContentType());
        fileEntity.setData(file.getBytes());
        fileEntity.setSize(file.getSize());
        fileEntity.setComment(comment);
        fileEntity.setCreateDate();
        fileEntity.setPlugin(plugin);
        fileEntity.setUserInfos(userInfos);
        fileEntity.setLabel(label);

        fileRepository.save(fileEntity);
    }
    
    public void delete(String id) {
        fileRepository.deleteById(id);
    }

    public Optional<FileEntity> getFile(String id) {
        return fileRepository.findById(id);
    }

    public List<FileEntity> getAllFiles() {
        return fileRepository.findAll();
    }

    public List<FileEntity> findByPlugin(String plugin) {
        return fileRepository.findByPlugin(plugin);
    }
}
