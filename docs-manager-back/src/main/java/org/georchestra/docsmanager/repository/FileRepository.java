package org.georchestra.docsmanager.repository;

import java.util.List;
import org.georchestra.docsmanager.model.FileEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, String> {
    Boolean existsByLabelLike(String label);

    Boolean existsByIdLike(String id);

    List<FileEntity> findByOpenedAndEntity(Boolean opened, String entity, Sort sort);

    List<FileEntity> findByEntityLike(String entity, Sort sort);

    List<FileEntity> findByEntityAndPlugin(String entity, String plugin, Sort sort);

    List<FileEntity> findByStatusAndPlugin(String status, String plugin, Sort sort);

    List<FileEntity> findByOpenedAndPlugin(Boolean opened, String plugin, Sort sort);

    List<FileEntity> findByOpenedAndStatusAndPlugin(Boolean opened, String status, String plugin, Sort sort);

    List<FileEntity> findByOpenedAndPluginAndEntity(Boolean opened, String plugin, String entity, Sort sort);
}
