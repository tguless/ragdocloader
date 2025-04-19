package com.docloader.repository;

import com.docloader.model.DocumentJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentJobRepository extends JpaRepository<DocumentJob, UUID> {
    List<DocumentJob> findByCreatedBy(UUID userId);
    
    List<DocumentJob> findByStatus(DocumentJob.JobStatus status);
    
    List<DocumentJob> findByScheduledTimeBefore(LocalDateTime time);
    
    List<DocumentJob> findByStatusAndScheduledTimeBefore(DocumentJob.JobStatus status, LocalDateTime time);
} 