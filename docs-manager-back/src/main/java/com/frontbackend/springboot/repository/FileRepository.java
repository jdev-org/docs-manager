package com.frontbackend.springboot.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.frontbackend.springboot.model.FileEntity;

import jakarta.transaction.Transactional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, String> {
    @Transactional
    @Query(value="SELECT * FROM files u WHERE u.plugin = :plugin", nativeQuery=true)
    List<FileEntity> findByPlugin(String plugin);
}
