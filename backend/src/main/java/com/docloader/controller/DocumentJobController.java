package com.docloader.controller;

import com.docloader.dto.DocumentJobRequest;
import com.docloader.model.DocumentJob;
import com.docloader.service.AuthService;
import com.docloader.service.DocumentJobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
public class DocumentJobController {

    private final DocumentJobService documentJobService;
    private final AuthService authService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<DocumentJob>> getAllJobs() {
        try {
            UUID userId = authService.getCurrentUserId();
            log.info("Getting all jobs for user ID: {}", userId);
            
            List<DocumentJob> jobs = documentJobService.getAllJobsByUserId(userId);
            log.info("Found {} jobs for user ID: {}", jobs.size(), userId);
            
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            log.error("Error getting all jobs: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SYSTEM_ADMIN')")
    public ResponseEntity<DocumentJob> getJobById(@PathVariable UUID id) {
        try {
            log.info("Getting job by ID: {}", id);
            
            return documentJobService.getJobById(id)
                    .map(job -> {
                        // Check if user has access to this job
                        UUID currentUserId = authService.getCurrentUserId();
                        log.info("Current user ID: {}, job creator ID: {}", currentUserId, job.getCreatedBy());
                        
                        if (currentUserId.equals(job.getCreatedBy()) || authService.getCurrentUser().getAuthorities().stream()
                                .anyMatch(a -> {
                                    String authority = a.getAuthority();
                                    log.debug("User has authority: {}", authority);
                                    return authority.equals("ROLE_ADMIN") || authority.equals("ROLE_SYSTEM_ADMIN");
                                })) {
                            log.info("User has access to job ID: {}", id);
                            return ResponseEntity.ok(job);
                        } else {
                            log.warn("User {} does not have access to job ID: {}", currentUserId, id);
                            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to this job");
                        }
                    })
                    .orElseThrow(() -> {
                        log.warn("Job not found with ID: {}", id);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found");
                    });
        } catch (Exception e) {
            log.error("Error getting job by ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SYSTEM_ADMIN')")
    public ResponseEntity<DocumentJob> createJob(@Valid @RequestBody DocumentJobRequest jobRequest) {
        try {
            UUID currentUserId = authService.getCurrentUserId();
            log.info("Creating job for user ID: {}", currentUserId);
            
            DocumentJob job = new DocumentJob();
            job.setName(jobRequest.getName());
            job.setDescription(jobRequest.getDescription());
            job.setSourceLocation(jobRequest.getSourceLocation());
            job.setCreatedBy(currentUserId);
            
            if (jobRequest.getScheduledTime() != null) {
                job.setStatus(DocumentJob.JobStatus.SCHEDULED);
                job.setScheduledTime(jobRequest.getScheduledTime());
            } else {
                job.setStatus(DocumentJob.JobStatus.PENDING);
            }
            
            DocumentJob createdJob = documentJobService.createJob(job);
            log.info("Job created successfully with ID: {}", createdJob.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(createdJob);
        } catch (Exception e) {
            log.error("Error creating job: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating job");
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SYSTEM_ADMIN')")
    public ResponseEntity<DocumentJob> updateJob(@PathVariable UUID id, @Valid @RequestBody DocumentJobRequest jobRequest) {
        try {
            log.info("Updating job with ID: {}", id);
            
            DocumentJob existingJob = documentJobService.getJobById(id)
                    .orElseThrow(() -> {
                        log.warn("Job not found with ID: {}", id);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found");
                    });
            
            // Check if user has access to update this job
            UUID currentUserId = authService.getCurrentUserId();
            log.info("Current user ID: {}, job creator ID: {}", currentUserId, existingJob.getCreatedBy());
            
            if (!currentUserId.equals(existingJob.getCreatedBy()) && authService.getCurrentUser().getAuthorities().stream()
                    .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SYSTEM_ADMIN"))) {
                log.warn("User {} does not have access to update job ID: {}", currentUserId, id);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to update this job");
            }
            
            // Only allow updates if job is not in progress
            if (existingJob.getStatus() == DocumentJob.JobStatus.PROCESSING) {
                log.warn("Cannot update job ID: {} because it is in progress", id);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update a job that is in progress");
            }
            
            existingJob.setName(jobRequest.getName());
            existingJob.setDescription(jobRequest.getDescription());
            
            // Only allow changing source location if job hasn't been processed yet
            if (existingJob.getStatus() == DocumentJob.JobStatus.PENDING || existingJob.getStatus() == DocumentJob.JobStatus.SCHEDULED) {
                existingJob.setSourceLocation(jobRequest.getSourceLocation());
            }
            
            // Update scheduling
            if (jobRequest.getScheduledTime() != null && 
                (existingJob.getStatus() == DocumentJob.JobStatus.PENDING || existingJob.getStatus() == DocumentJob.JobStatus.SCHEDULED)) {
                existingJob.setStatus(DocumentJob.JobStatus.SCHEDULED);
                existingJob.setScheduledTime(jobRequest.getScheduledTime());
            }
            
            DocumentJob updatedJob = documentJobService.updateJob(id, existingJob);
            log.info("Job updated successfully: {}", id);
            
            return ResponseEntity.ok(updatedJob);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating job: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating job");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SYSTEM_ADMIN')")
    public ResponseEntity<Void> deleteJob(@PathVariable UUID id) {
        try {
            log.info("Deleting job with ID: {}", id);
            
            DocumentJob job = documentJobService.getJobById(id)
                    .orElseThrow(() -> {
                        log.warn("Job not found with ID: {}", id);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found");
                    });
            
            // Check if user has access to delete this job
            UUID currentUserId = authService.getCurrentUserId();
            log.info("Current user ID: {}, job creator ID: {}", currentUserId, job.getCreatedBy());
            
            if (!currentUserId.equals(job.getCreatedBy()) && authService.getCurrentUser().getAuthorities().stream()
                    .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SYSTEM_ADMIN"))) {
                log.warn("User {} does not have access to delete job ID: {}", currentUserId, id);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to delete this job");
            }
            
            // Only allow deletion if job is not in progress
            if (job.getStatus() == DocumentJob.JobStatus.PROCESSING) {
                log.warn("Cannot delete job ID: {} because it is in progress", id);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete a job that is in progress");
            }
            
            documentJobService.deleteJob(id);
            log.info("Job deleted successfully: {}", id);
            
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting job: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting job");
        }
    }
} 