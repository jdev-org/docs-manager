package org.georchestra.docsmanager.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.georchestra.docsmanager.model.FileEntity;
import org.georchestra.docsmanager.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    private final FileRepository fileRepository;

    @Autowired
    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public void save(MultipartFile file, String plugin, String comment, String userInfos,
            String label, String dateDoc, String status) throws IOException {
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
        fileEntity.setDateDoc(dateDoc);
        fileEntity.setStatus(status);

        fileRepository.save(fileEntity);
    }

    public void delete(String id) {
        fileRepository.deleteById(id);
    }

    public Optional<FileEntity> getFile(String id) {
        return fileRepository.findById(id);
    }

    public List<FileEntity> getAllFiles() {
        return fileRepository.findAll(Sort.by("plugin"));
    }

    public List<FileEntity> getAllFilesFromExample(FileEntity file) {
        return fileRepository.findAll(Example.of(file));
    }

    public List<FileEntity> getFileByLabel(String label) {
        return fileRepository.findByLabel(label);
    }
}
