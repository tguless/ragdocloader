package com.docloader.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentJobRequest {
    
    @NotBlank
    @Size(min = 3, max = 100)
    private String name;
    
    @Size(max = 500)
    private String description;
    
    @NotBlank
    @Size(min = 3, max = 1000)
    private String sourceLocation;
    
    private LocalDateTime scheduledTime;
    
    // New fields for frontend integration
    private String type;
    
    private Map<String, Object> config;
    
    private String sourceType; // "s3" or "upload"
    
    private UUID s3BucketId;
    
    private String s3SourcePath;
    
    private List<UUID> documentIds;
} 