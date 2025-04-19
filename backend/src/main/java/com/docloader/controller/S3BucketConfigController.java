package com.docloader.controller;

import com.docloader.dto.S3BucketConfigRequest;
import com.docloader.dto.S3BucketConfigResponse;
import com.docloader.model.S3BucketConfig;
import com.docloader.multitenancy.TenantContext;
import com.docloader.service.S3BucketConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tenants/{tenantId}/s3-configs")
@RequiredArgsConstructor
@Tag(name = "S3 Bucket Configurations", description = "API endpoints for managing S3 bucket configurations")
public class S3BucketConfigController {

    private final S3BucketConfigService s3BucketConfigService;

    @GetMapping
    @Operation(summary = "Get all S3 bucket configurations for a tenant")
    @PreAuthorize("@securityService.isTenantAdmin(#tenantId) or @securityService.isSystemAdmin()")
    public ResponseEntity<List<S3BucketConfigResponse>> getAllBucketConfigs(@PathVariable UUID tenantId) {
        List<S3BucketConfig> configs = s3BucketConfigService.getAllBucketConfigs(tenantId);
        List<S3BucketConfigResponse> responses = configs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{configId}")
    @Operation(summary = "Get S3 bucket configuration by ID")
    @PreAuthorize("@securityService.isTenantAdmin(#tenantId) or @securityService.isSystemAdmin()")
    public ResponseEntity<S3BucketConfigResponse> getBucketConfigById(
            @PathVariable UUID tenantId, 
            @PathVariable UUID configId) {
        return s3BucketConfigService.getBucketConfigById(tenantId, configId)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException("S3 bucket configuration not found"));
    }

    @GetMapping("/default")
    @Operation(summary = "Get default S3 bucket configuration for a tenant")
    @PreAuthorize("@securityService.isTenantAdmin(#tenantId) or @securityService.isSystemAdmin()")
    public ResponseEntity<S3BucketConfigResponse> getDefaultBucketConfig(@PathVariable UUID tenantId) {
        return s3BucketConfigService.getDefaultBucketConfig(tenantId)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException("No default S3 bucket configuration found"));
    }

    @PostMapping
    @Operation(summary = "Create a new S3 bucket configuration")
    @PreAuthorize("@securityService.isTenantAdmin(#tenantId) or @securityService.isSystemAdmin()")
    public ResponseEntity<S3BucketConfigResponse> createBucketConfig(
            @PathVariable UUID tenantId,
            @Valid @RequestBody S3BucketConfigRequest request) {
        S3BucketConfig config = s3BucketConfigService.createBucketConfig(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(config));
    }

    @PutMapping("/{configId}")
    @Operation(summary = "Update an existing S3 bucket configuration")
    @PreAuthorize("@securityService.isTenantAdmin(#tenantId) or @securityService.isSystemAdmin()")
    public ResponseEntity<S3BucketConfigResponse> updateBucketConfig(
            @PathVariable UUID tenantId,
            @PathVariable UUID configId,
            @Valid @RequestBody S3BucketConfigRequest request) {
        S3BucketConfig config = s3BucketConfigService.updateBucketConfig(tenantId, configId, request);
        return ResponseEntity.ok(toResponse(config));
    }

    @DeleteMapping("/{configId}")
    @Operation(summary = "Delete an S3 bucket configuration")
    @PreAuthorize("@securityService.isTenantAdmin(#tenantId) or @securityService.isSystemAdmin()")
    public ResponseEntity<Void> deleteBucketConfig(
            @PathVariable UUID tenantId,
            @PathVariable UUID configId) {
        s3BucketConfigService.deleteBucketConfig(tenantId, configId);
        return ResponseEntity.noContent().build();
    }

    private S3BucketConfigResponse toResponse(S3BucketConfig config) {
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