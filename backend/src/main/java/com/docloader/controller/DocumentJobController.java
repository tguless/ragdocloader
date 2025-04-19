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
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<DocumentJob>> getAllJobs() {
        UUID userId = authService.getCurrentUserId();
        return ResponseEntity.ok(documentJobService.getAllJobsByUserId(userId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<DocumentJob> getJobById(@PathVariable UUID id) {
        return documentJobService.getJobById(id)
                .map(job -> {
                    // Check if user has access to this job
                    UUID currentUserId = authService.getCurrentUserId();
                    if (currentUserId.equals(job.getCreatedBy()) || authService.getCurrentUser().getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                        return ResponseEntity.ok(job);
                    } else {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to this job");
                    }
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<DocumentJob> createJob(@Valid @RequestBody DocumentJobRequest jobRequest) {
        try {
            UUID currentUserId = authService.getCurrentUserId();
            
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
            return ResponseEntity.status(HttpStatus.CREATED).body(createdJob);
        } catch (Exception e) {
            log.error("Error creating job: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating job");
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<DocumentJob> updateJob(@PathVariable UUID id, @Valid @RequestBody DocumentJobRequest jobRequest) {
        try {
            DocumentJob existingJob = documentJobService.getJobById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
            
            // Check if user has access to update this job
            UUID currentUserId = authService.getCurrentUserId();
            if (!currentUserId.equals(existingJob.getCreatedBy()) && authService.getCurrentUser().getAuthorities().stream()
                    .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to update this job");
            }
            
            // Only allow updates if job is not in progress
            if (existingJob.getStatus() == DocumentJob.JobStatus.PROCESSING) {
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
            return ResponseEntity.ok(updatedJob);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating job: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating job");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> deleteJob(@PathVariable UUID id) {
        try {
            DocumentJob job = documentJobService.getJobById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
            
            // Check if user has access to delete this job
            UUID currentUserId = authService.getCurrentUserId();
            if (!currentUserId.equals(job.getCreatedBy()) && authService.getCurrentUser().getAuthorities().stream()
                    .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to delete this job");
            }
            
            // Only allow deletion if job is not in progress
            if (job.getStatus() == DocumentJob.JobStatus.PROCESSING) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete a job that is in progress");
            }
            
            documentJobService.deleteJob(id);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting job: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting job");
        }
    }
} 