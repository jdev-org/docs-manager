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
            String label, String dateDoc, String status, String entity, Boolean opened) throws IOException {
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
        fileEntity.setEntity(entity);
        fileEntity.setOpened(opened);

        fileRepository.save(fileEntity);
    }
    public void update(FileEntity fileToUpdate) throws IOException {    
        Optional<FileEntity> optionalFileToUp = fileRepository.findById(fileToUpdate.getId());

        FileEntity existingFile = optionalFileToUp.get();
        
        existingFile.setComment(fileToUpdate.getComment());
        existingFile.setLabel(fileToUpdate.getLabel());
        existingFile.setStatus(fileToUpdate.getStatus());
        existingFile.setEntity(fileToUpdate.getEntity());
        existingFile.setOpened(fileToUpdate.getOpened());
        if(fileToUpdate.getDateDoc() != null) {
            existingFile.setDateDoc(fileToUpdate.getDateDoc());
        }

        fileRepository.save(existingFile);
    }

    public void delete(String id) {
        fileRepository.deleteById(id);
    }

    public Optional<FileEntity> getFile(String id) {
        return fileRepository.findById(id);
    }

    public Boolean existsByIdLike(String id) {
        return fileRepository.existsByIdLike(id);
    }

    public List<FileEntity> getAllFiles() {
        return fileRepository.findAll(Sort.by("plugin"));
    }

    public List<FileEntity> getAllFilesByPlugin() {
        return fileRepository.findAll(Sort.by("plugin"));
    }

    public List<FileEntity> getAllFilesFromExample(FileEntity file) {
        return fileRepository.findAll(Example.of(file));
    }

    public List<FileEntity> getPublicFilesByEntity(String entity) {
        return fileRepository.findByOpenedAndEntity(true, entity, Sort.by("label"));
    }

    public List<FileEntity> getAllFilesByEntity(String entity) {
        return fileRepository.findByEntityLike(entity, Sort.by("label"));
    }

    public List<FileEntity> getAllFilesByEntityAndPlugin(String entity, String plugin) {
        return fileRepository.findByEntityAndPlugin(entity, plugin, Sort.by("label"));
    }

    public List<FileEntity> getPublicFilesByPlugin(String plugin) {
        return fileRepository.findByOpenedAndPlugin(true, plugin, Sort.by("label"));
    }

    public List<FileEntity> getPublicFilesByPluginAndStatus(String plugin, String status) {
        return fileRepository.findByOpenedAndStatusAndPlugin(true, status, plugin, Sort.by("label"));
    }

    public List<FileEntity> getPublicFilesByPluginAndEntity(String plugin, String entity) {
        return fileRepository.findByOpenedAndPluginAndEntity(true, plugin, entity,  Sort.by("label"));
    }

    public Boolean existsByLabel(String label) {
        return fileRepository.existsByLabelLike(label);
    }
}
