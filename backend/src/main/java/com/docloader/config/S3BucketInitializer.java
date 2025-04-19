package com.docloader.config;

import com.docloader.model.Tenant;
import com.docloader.service.S3Service;
import com.docloader.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Initializes the S3 buckets for all tenants on application startup
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class S3BucketInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final S3Service s3Service;
    private final TenantService tenantService;
    
    @Qualifier("s3BucketName")
    private final String defaultBucketName;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // Initialize default bucket for backward compatibility
        log.info("Initializing default S3 bucket: {}", defaultBucketName);
        try {
            s3Service.createBucketIfNotExists(defaultBucketName);
            log.info("Default S3 bucket initialization completed successfully");
        } catch (Exception e) {
            log.error("Failed to initialize default S3 bucket: {}", e.getMessage(), e);
        }
        
        // Initialize tenant-specific buckets
        log.info("Initializing tenant-specific S3 buckets");
        try {
            List<Tenant> tenants = tenantService.getAllTenants();
            log.info("Found {} tenants", tenants.size());
            
            for (Tenant tenant : tenants) {
                try {
                    log.info("Creating S3 bucket for tenant '{}' ({})", 
                           tenant.getName(), tenant.getS3BucketName());
                    s3Service.createBucketIfNotExists(tenant);
                    log.info("Successfully created S3 bucket for tenant '{}'", tenant.getName());
                } catch (Exception e) {
                    log.error("Failed to initialize S3 bucket for tenant '{}': {}", 
                            tenant.getName(), e.getMessage(), e);
                }
            }
            log.info("Tenant-specific S3 bucket initialization completed");
        } catch (Exception e) {
            log.error("Failed to initialize tenant-specific S3 buckets: {}", e.getMessage(), e);
        }
    }
} 