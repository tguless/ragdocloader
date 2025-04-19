package com.docloader.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class S3BucketConfigRequest {
    
    @NotBlank
    @Size(min = 3, max = 50)
    private String name;
    
    @NotBlank
    @Size(min = 3, max = 50)
    private String bucketName;
    
    private Boolean isDefault = false;
    
    @Size(max = 255)
    private String endpoint;
    
    @Size(max = 50)
    private String region;
    
    @Size(max = 255)
    private String accessKey;
    
    @Size(max = 255)
    private String secretKey;
    
    private Boolean pathStyleAccess = false;
} 