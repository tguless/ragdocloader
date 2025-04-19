package com.docloader.service;

import com.docloader.model.DocumentJob;
import com.docloader.repository.DocumentJobRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentJobService {

    private final DocumentJobRepository documentJobRepository;

    public List<DocumentJob> getAllJobs() {
        return documentJobRepository.findAll();
    }

    public List<DocumentJob> getAllJobsByUserId(UUID userId) {
        return documentJobRepository.findByCreatedBy(userId);
    }

    public List<DocumentJob> getJobsByStatus(DocumentJob.JobStatus status) {
        return documentJobRepository.findByStatus(status);
    }
    
    public List<DocumentJob> getScheduledJobsDue() {
        return documentJobRepository.findByStatusAndScheduledTimeBefore(
                DocumentJob.JobStatus.SCHEDULED, 
                LocalDateTime.now());
    }

    public Optional<DocumentJob> getJobById(UUID id) {
        return documentJobRepository.findById(id);
    }

    @Transactional
    public DocumentJob createJob(DocumentJob job) {
        log.info("Creating new document job: {}", job.getName());
        return documentJobRepository.save(job);
    }

    @Transactional
    public DocumentJob updateJob(UUID id, DocumentJob jobDetails) {
        log.info("Updating document job: {}", id);
        
        DocumentJob job = documentJobRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Job not found with id: " + id));
        
        job.setName(jobDetails.getName());
        job.setDescription(jobDetails.getDescription());
        job.setSourceLocation(jobDetails.getSourceLocation());
        job.setStatus(jobDetails.getStatus());
        job.setScheduledTime(jobDetails.getScheduledTime());
        
        return documentJobRepository.save(job);
    }

    @Transactional
    public void deleteJob(UUID id) {
        log.info("Deleting document job: {}", id);
        
        DocumentJob job = documentJobRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Job not found with id: " + id));
        
        documentJobRepository.delete(job);
    }
    
    @Transactional
    public DocumentJob updateJobStatus(UUID id, DocumentJob.JobStatus status) {
        log.info("Updating job status to {} for job: {}", status, id);
        
        DocumentJob job = documentJobRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Job not found with id: " + id));
        
        job.setStatus(status);
        
        if (status == DocumentJob.JobStatus.COMPLETED) {
            job.setCompletedTime(LocalDateTime.now());
        }
        
        return documentJobRepository.save(job);
    }
} 