package com.docloader.dto;

import com.docloader.model.S3BucketConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class S3BucketConfigResponse {
    
    private UUID id;
    private String name;
    private String bucketName;
    private Boolean isDefault;
    private String endpoint;
    private String region;
    // We don't include secretKey in responses for security reasons
    private String accessKey;
    private Boolean pathStyleAccess;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static S3BucketConfigResponse fromEntity(S3BucketConfig config) {
        S3BucketConfigResponse response = new S3BucketConfigResponse();
        response.setId(config.getId());
        response.setName(config.getName());
        response.setBucketName(config.getBucketName());
        response.setIsDefault(config.getIsDefault());
        response.setEndpoint(config.getEndpoint());
        response.setRegion(config.getRegion());
        response.setAccessKey(config.getAccessKey());
        response.setPathStyleAccess(config.getPathStyleAccess());
        response.setCreatedAt(config.getCreatedAt());
        response.setUpdatedAt(config.getUpdatedAt());
        return response;
    }
} 