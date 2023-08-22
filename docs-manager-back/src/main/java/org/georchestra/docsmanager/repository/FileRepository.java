package org.georchestra.docsmanager.repository;

import org.georchestra.docsmanager.model.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, String> {
    Boolean existsByLabelLike(String label);

    Boolean existsByIdLike(String id);
}
