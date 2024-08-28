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

    List<FileEntity> findByStatusAndEntity(String status, String entity, Sort sort);

    List<FileEntity> findByStatusAndPlugin(String status, String plugin, Sort sort);

    List<FileEntity> findByStatusAndPluginAndEntity(String status, String plugin, String entity, Sort sort);
}
