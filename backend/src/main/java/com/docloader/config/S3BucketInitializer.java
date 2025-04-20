package com.docloader.config;

import com.docloader.model.Tenant;
import com.docloader.service.S3BucketConfigService;
import com.docloader.service.S3Service;
import com.docloader.service.TenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Initializes the S3 buckets for all tenants on application startup
 */
@Component
@Slf4j
public class S3BucketInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final S3Service s3Service;
    private final TenantService tenantService;
    private final S3BucketConfigService s3BucketConfigService;
    
    @Qualifier("s3BucketName")
    private final String defaultBucketName;

    // Use constructor injection with @Lazy to break circular dependency
    public S3BucketInitializer(
            @Lazy S3Service s3Service,
            @Lazy TenantService tenantService,
            @Lazy S3BucketConfigService s3BucketConfigService,
            @Qualifier("s3BucketName") String defaultBucketName) {
        this.s3Service = s3Service;
        this.tenantService = tenantService;
        this.s3BucketConfigService = s3BucketConfigService;
        this.defaultBucketName = defaultBucketName;
    }

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
                    log.info("Creating S3 bucket configuration for tenant '{}'", tenant.getName());
                    s3BucketConfigService.createDefaultBucketConfig(tenant);
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