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
    
    /**
     * Find documents by job ID
     */
    @Query("SELECT d FROM Document d WHERE d.job.id = :jobId")
    List<Document> findByJobId(UUID jobId);
    
    /**
     * Find documents uploaded by a specific user
     */
    List<Document> findByUploadedBy(UUID uploadedBy);
    
    /**
     * Find documents by status
     */
    List<Document> findByStatus(Document.DocumentStatus status);
    
    /**
     * Count documents for a job
     */
    long countByJobId(UUID jobId);
    
    /**
     * Get count of documents uploaded by a user
     */
    long countByUploadedBy(UUID uploadedBy);
    
    List<Document> findByJob(DocumentJob job);
    
    Optional<Document> findByFilePathAndMd5Hash(String filePath, String md5Hash);
    
    boolean existsByFilePathAndMd5Hash(String filePath, String md5Hash);
    
    @Query("SELECT COUNT(d) FROM Document d WHERE d.job.id = ?1 AND d.status = ?2")
    long countByJobIdAndStatus(UUID jobId, Document.DocumentStatus status);
} 