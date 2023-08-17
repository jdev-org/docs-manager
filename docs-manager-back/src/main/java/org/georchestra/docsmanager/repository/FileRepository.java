package org.georchestra.docsmanager.repository;

import java.util.List;
import org.georchestra.docsmanager.model.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, String> {
    @Transactional
    @Query(value = "SELECT * FROM files u WHERE u.label like %:label% LIMIT 1", nativeQuery = true)
    List<FileEntity> findByLabel(String label);
}
