package com.docloader.repository;

import com.docloader.model.Document;
import com.docloader.model.DocumentJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByJobId(UUID jobId);
    
    List<Document> findByJob(DocumentJob job);
    
    Optional<Document> findByFilePathAndMd5Hash(String filePath, String md5Hash);
    
    boolean existsByFilePathAndMd5Hash(String filePath, String md5Hash);
    
    @Query("SELECT COUNT(d) FROM Document d WHERE d.job.id = ?1")
    long countByJobId(UUID jobId);
    
    @Query("SELECT COUNT(d) FROM Document d WHERE d.job.id = ?1 AND d.status = ?2")
    long countByJobIdAndStatus(UUID jobId, Document.DocumentStatus status);
} 