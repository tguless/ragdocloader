package com.docloader.dto;

import com.docloader.model.Document;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentResponse {
    
    private UUID id;
    
    private UUID jobId;
    
    private String filename;
    
    private String contentType;
    
    private Long fileSize;
    
    private Document.DocumentStatus status;
    
    private UUID uploadedBy;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime processedAt;
} 