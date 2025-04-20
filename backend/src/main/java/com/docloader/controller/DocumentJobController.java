package com.docloader.controller;

import com.docloader.dto.DocumentJobRequest;
import com.docloader.dto.DocumentJobResponse;
import com.docloader.model.DocumentJob;
import com.docloader.model.S3BucketConfig;
import com.docloader.service.AuthService;
import com.docloader.service.DocumentJobService;
import com.docloader.service.S3BucketConfigService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
public class DocumentJobController {

    private final DocumentJobService documentJobService;
    private final AuthService authService;
    private final S3BucketConfigService s3BucketConfigService;
    private final ObjectMapper objectMapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<DocumentJobResponse>> getAllJobs() {
        try {
            UUID userId = authService.getCurrentUserId();
            log.info("Getting all jobs for user ID: {}", userId);
            
            List<DocumentJob> jobs = documentJobService.getAllJobsByUserId(userId);
            log.info("Found {} jobs for user ID: {}", jobs.size(), userId);
            
            List<DocumentJobResponse> responses = jobs.stream()
                    .map(this::toDocumentJobResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting all jobs: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SYSTEM_ADMIN')")
    public ResponseEntity<DocumentJobResponse> getJobById(@PathVariable UUID id) {
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
                            return ResponseEntity.ok(toDocumentJobResponse(job));
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
    public ResponseEntity<DocumentJobResponse> createJob(@Valid @RequestBody DocumentJobRequest jobRequest) {
        try {
            UUID currentUserId = authService.getCurrentUserId();
            log.info("Creating job for user ID: {}", currentUserId);
            
            DocumentJob job = new DocumentJob();
            job.setName(jobRequest.getName());
            job.setDescription(jobRequest.getDescription());
            job.setCreatedBy(currentUserId);
            
            // Handle job type and config
            if (jobRequest.getType() != null) {
                job.setJobType(jobRequest.getType());
            }
            
            if (jobRequest.getConfig() != null) {
                try {
                    job.setConfigJson(objectMapper.writeValueAsString(jobRequest.getConfig()));
                } catch (JsonProcessingException e) {
                    log.error("Error serializing config to JSON: {}", e.getMessage(), e);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid configuration format");
                }
            }
            
            // Handle source based on type
            if ("s3".equalsIgnoreCase(jobRequest.getSourceType())) {
                job.setSourceType(DocumentJob.SourceType.S3);
                
                if (jobRequest.getS3BucketId() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "S3 bucket ID is required for S3 source type");
                }
                
                // Get S3 bucket config
                S3BucketConfig bucketConfig = s3BucketConfigService.getBucketConfigById(authService.getCurrentUser().getTenantId(), jobRequest.getS3BucketId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "S3 bucket configuration not found"));
                
                job.setS3BucketConfig(bucketConfig);
                
                // Set source location as path within bucket
                String path = jobRequest.getS3SourcePath();
                if (path == null || path.isEmpty()) {
                    path = "/";
                }
                job.setSourceLocation(path);
            } else if ("upload".equalsIgnoreCase(jobRequest.getSourceType())) {
                job.setSourceType(DocumentJob.SourceType.UPLOAD);
                
                if (jobRequest.getDocumentIds() == null || jobRequest.getDocumentIds().isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document IDs are required for upload source type");
                }
                
                // For direct uploads, source location will be a comma-separated list of document IDs
                job.setSourceLocation(String.join(",", jobRequest.getDocumentIds().stream().map(UUID::toString).collect(Collectors.toList())));
            } else {
                // Default behavior for backward compatibility
                job.setSourceLocation(jobRequest.getSourceLocation());
            }
            
            // Handle scheduling
            if (jobRequest.getScheduledTime() != null) {
                job.setStatus(DocumentJob.JobStatus.SCHEDULED);
                job.setScheduledTime(jobRequest.getScheduledTime());
            } else {
                job.setStatus(DocumentJob.JobStatus.PENDING);
            }
            
            DocumentJob createdJob = documentJobService.createJob(job);
            log.info("Job created successfully with ID: {}", createdJob.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(toDocumentJobResponse(createdJob));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating job: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating job");
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SYSTEM_ADMIN')")
    public ResponseEntity<DocumentJobResponse> updateJob(@PathVariable UUID id, @Valid @RequestBody DocumentJobRequest jobRequest) {
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
            
            // Handle job type and config updates if job hasn't been processed yet
            if (existingJob.getStatus() == DocumentJob.JobStatus.PENDING || existingJob.getStatus() == DocumentJob.JobStatus.SCHEDULED) {
                if (jobRequest.getType() != null) {
                    existingJob.setJobType(jobRequest.getType());
                }
                
                if (jobRequest.getConfig() != null) {
                    try {
                        existingJob.setConfigJson(objectMapper.writeValueAsString(jobRequest.getConfig()));
                    } catch (JsonProcessingException e) {
                        log.error("Error serializing config to JSON: {}", e.getMessage(), e);
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid configuration format");
                    }
                }
                
                // Handle source updates
                if ("s3".equalsIgnoreCase(jobRequest.getSourceType())) {
                    existingJob.setSourceType(DocumentJob.SourceType.S3);
                    
                    if (jobRequest.getS3BucketId() != null) {
                        // Get S3 bucket config
                        S3BucketConfig bucketConfig = s3BucketConfigService.getBucketConfigById(
                                authService.getCurrentUser().getTenantId(), jobRequest.getS3BucketId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                                        "S3 bucket configuration not found"));
                        
                        existingJob.setS3BucketConfig(bucketConfig);
                    }
                    
                    // Update source location as path within bucket
                    if (jobRequest.getS3SourcePath() != null) {
                        existingJob.setSourceLocation(jobRequest.getS3SourcePath());
                    }
                } else if ("upload".equalsIgnoreCase(jobRequest.getSourceType())) {
                    existingJob.setSourceType(DocumentJob.SourceType.UPLOAD);
                    
                    if (jobRequest.getDocumentIds() != null && !jobRequest.getDocumentIds().isEmpty()) {
                        // For direct uploads, source location will be a comma-separated list of document IDs
                        existingJob.setSourceLocation(String.join(",", 
                                jobRequest.getDocumentIds().stream().map(UUID::toString).collect(Collectors.toList())));
                    }
                } else if (jobRequest.getSourceLocation() != null) {
                    // Default behavior for backward compatibility
                    existingJob.setSourceLocation(jobRequest.getSourceLocation());
                }
            }
            
            // Update scheduling
            if (jobRequest.getScheduledTime() != null && 
                (existingJob.getStatus() == DocumentJob.JobStatus.PENDING || existingJob.getStatus() == DocumentJob.JobStatus.SCHEDULED)) {
                existingJob.setStatus(DocumentJob.JobStatus.SCHEDULED);
                existingJob.setScheduledTime(jobRequest.getScheduledTime());
            }
            
            DocumentJob updatedJob = documentJobService.updateJob(id, existingJob);
            log.info("Job updated successfully: {}", id);
            
            return ResponseEntity.ok(toDocumentJobResponse(updatedJob));
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
    
    private DocumentJobResponse toDocumentJobResponse(DocumentJob job) {
        DocumentJobResponse response = new DocumentJobResponse();
        response.setId(job.getId());
        response.setName(job.getName());
        response.setDescription(job.getDescription());
        response.setSourceLocation(job.getSourceLocation());
        response.setStatus(job.getStatus());
        response.setJobType(job.getJobType());
        response.setSourceType(job.getSourceType());
        response.setCreatedBy(job.getCreatedBy());
        response.setCreatedAt(job.getCreatedAt());
        response.setUpdatedAt(job.getUpdatedAt());
        response.setScheduledTime(job.getScheduledTime());
        response.setCompletedTime(job.getCompletedTime());
        
        // Convert config JSON to map if exists
        if (job.getConfigJson() != null && !job.getConfigJson().isEmpty()) {
            try {
                response.setConfig(objectMapper.readValue(job.getConfigJson(), Map.class));
            } catch (JsonProcessingException e) {
                log.error("Error deserializing config JSON: {}", e.getMessage(), e);
            }
        }
        
        // Include S3 bucket config if exists
        if (job.getS3BucketConfig() != null) {
            response.setS3BucketConfig(s3BucketConfigService.toResponse(job.getS3BucketConfig()));
        }
        
        return response;
    }
} 