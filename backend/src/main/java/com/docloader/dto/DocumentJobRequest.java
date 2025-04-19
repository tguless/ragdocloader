package com.docloader.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
} 