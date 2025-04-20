package com.docloader.dto;

import com.docloader.model.DocumentJob;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentJobResponse {
    
    private UUID id;
    
    private String name;
    
    private String description;
    
    private String sourceLocation;
    
    private DocumentJob.JobStatus status;
    
    private String jobType;
    
    private Map<String, Object> config;
    
    private DocumentJob.SourceType sourceType;
    
    private S3BucketConfigResponse s3BucketConfig;
    
    private UUID createdBy;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime scheduledTime;
    
    private LocalDateTime completedTime;
} 