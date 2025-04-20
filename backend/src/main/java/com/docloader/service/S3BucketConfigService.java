package com.docloader.service;

import com.docloader.dto.S3BucketConfigRequest;
import com.docloader.model.S3BucketConfig;
import com.docloader.model.Tenant;
import com.docloader.repository.S3BucketConfigRepository;
import com.docloader.repository.TenantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class S3BucketConfigService {
    
    private final S3BucketConfigRepository s3BucketConfigRepository;
    private final TenantRepository tenantRepository;
    private final S3Service s3Service;
    
    @Qualifier("s3BucketName")
    private final String defaultBucketName;
    
    @Value("${docloader.s3.endpoint:}")
    private String defaultEndpoint;
    
    @Value("${docloader.s3.region:us-east-1}")
    private String defaultRegion;
    
    @Value("${docloader.s3.access-key:}")
    private String defaultAccessKey;
    
    @Value("${docloader.s3.secret-key:}")
    private String defaultSecretKey;
    
    @Value("${docloader.s3.path-style-access:false}")
    private boolean defaultPathStyleAccess;
    
    // Use constructor injection with @Lazy to break circular dependency
    public S3BucketConfigService(
            S3BucketConfigRepository s3BucketConfigRepository,
            TenantRepository tenantRepository,
            @Lazy S3Service s3Service,
            @Qualifier("s3BucketName") String defaultBucketName) {
        this.s3BucketConfigRepository = s3BucketConfigRepository;
        this.tenantRepository = tenantRepository;
        this.s3Service = s3Service;
        this.defaultBucketName = defaultBucketName;
    }
    
    public List<S3BucketConfig> getAllBucketConfigs(UUID tenantId) {
        return s3BucketConfigRepository.findByTenantId(tenantId);
    }
    
    public Optional<S3BucketConfig> getBucketConfigById(UUID tenantId, UUID configId) {
        return s3BucketConfigRepository.findById(configId)
                .filter(config -> config.getTenant().getId().equals(tenantId));
    }
    
    public Optional<S3BucketConfig> getDefaultBucketConfig(UUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found with id: " + tenantId));
        
        return s3BucketConfigRepository.findByTenantAndIsDefaultTrue(tenant)
                .or(() -> s3BucketConfigRepository.findByTenant(tenant).stream().findFirst());
    }
    
    /**
     * Create a default S3 bucket configuration for a tenant if one doesn't exist
     * @param tenant The tenant to create a bucket configuration for
     * @return The created S3BucketConfig
     */
    @Transactional
    public S3BucketConfig createDefaultBucketConfig(Tenant tenant) {
        // Check if tenant already has bucket configs
        List<S3BucketConfig> existingConfigs = s3BucketConfigRepository.findByTenant(tenant);
        if (!existingConfigs.isEmpty()) {
            log.info("Tenant {} already has bucket configurations", tenant.getName());
            return existingConfigs.stream()
                    .filter(config -> Boolean.TRUE.equals(config.getIsDefault()))
                    .findFirst()
                    .orElse(existingConfigs.get(0));
        }
        
        // Create a new default bucket config
        String bucketName = tenant.getSubdomain().toLowerCase() + "-" + defaultBucketName;
        log.info("Creating default S3 bucket configuration for tenant: {} with bucket: {}", 
                tenant.getName(), bucketName);
        
        S3BucketConfig config = new S3BucketConfig();
        config.setTenant(tenant);
        config.setName("Default");
        config.setBucketName(bucketName);
        config.setEndpoint(defaultEndpoint);
        config.setRegion(defaultRegion);
        config.setAccessKey(defaultAccessKey);
        config.setSecretKey(defaultSecretKey);
        config.setPathStyleAccess(defaultPathStyleAccess);
        config.setIsDefault(true);
        
        S3BucketConfig savedConfig = s3BucketConfigRepository.save(config);
        
        // Try to create the bucket if it doesn't exist
        try {
            s3Service.createBucketIfNotExists(savedConfig);
            log.info("Successfully created S3 bucket for tenant: {}", tenant.getName());
        } catch (Exception e) {
            log.error("Failed to create S3 bucket for tenant '{}': {}", 
                    tenant.getName(), e.getMessage(), e);
            // Don't fail the bucket config creation if bucket creation fails
        }
        
        return savedConfig;
    }
    
    @Transactional
    public S3BucketConfig createBucketConfig(UUID tenantId, S3BucketConfigRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found with id: " + tenantId));
        
        // Validate unique name
        if (s3BucketConfigRepository.existsByTenantAndName(tenant, request.getName())) {
            throw new IllegalArgumentException("Bucket configuration with name '" + request.getName() + "' already exists for this tenant");
        }
        
        S3BucketConfig config = new S3BucketConfig();
        config.setTenant(tenant);
        config.setName(request.getName());
        config.setBucketName(request.getBucketName());
        config.setEndpoint(request.getEndpoint() != null ? request.getEndpoint() : defaultEndpoint);
        config.setRegion(request.getRegion() != null ? request.getRegion() : defaultRegion);
        config.setAccessKey(request.getAccessKey() != null ? request.getAccessKey() : defaultAccessKey);
        config.setSecretKey(request.getSecretKey() != null ? request.getSecretKey() : defaultSecretKey);
        config.setPathStyleAccess(request.getPathStyleAccess() != null ? request.getPathStyleAccess() : defaultPathStyleAccess);
        
        // Handle default configuration
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            // If this config is default, remove default flag from other configs
            s3BucketConfigRepository.findByTenantAndIsDefaultTrue(tenant)
                    .ifPresent(existingDefault -> {
                        existingDefault.setIsDefault(false);
                        s3BucketConfigRepository.save(existingDefault);
                    });
            config.setIsDefault(true);
        } else if (s3BucketConfigRepository.countByTenantAndIsDefaultTrue(tenant) == 0) {
            // If no default config exists, make this one default
            config.setIsDefault(true);
        } else {
            config.setIsDefault(request.getIsDefault());
        }
        
        S3BucketConfig savedConfig = s3BucketConfigRepository.save(config);
        
        // Try to create the bucket if it doesn't exist
        try {
            s3Service.createBucketIfNotExists(savedConfig);
        } catch (Exception e) {
            log.error("Failed to create S3 bucket for configuration '{}': {}", savedConfig.getName(), e.getMessage(), e);
            // Don't fail the bucket config creation if bucket creation fails
        }
        
        return savedConfig;
    }
    
    @Transactional
    public S3BucketConfig updateBucketConfig(UUID tenantId, UUID configId, S3BucketConfigRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found with id: " + tenantId));
        
        S3BucketConfig config = s3BucketConfigRepository.findById(configId)
                .filter(c -> c.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new EntityNotFoundException("Bucket configuration not found"));
        
        // Validate unique name if changed
        if (!config.getName().equals(request.getName()) && 
                s3BucketConfigRepository.existsByTenantAndName(tenant, request.getName())) {
            throw new IllegalArgumentException("Bucket configuration with name '" + request.getName() + "' already exists for this tenant");
        }
        
        config.setName(request.getName());
        config.setBucketName(request.getBucketName());
        
        if (request.getEndpoint() != null) {
            config.setEndpoint(request.getEndpoint());
        }
        
        if (request.getRegion() != null) {
            config.setRegion(request.getRegion());
        }
        
        if (request.getAccessKey() != null) {
            config.setAccessKey(request.getAccessKey());
        }
        
        if (request.getSecretKey() != null) {
            config.setSecretKey(request.getSecretKey());
        }
        
        if (request.getPathStyleAccess() != null) {
            config.setPathStyleAccess(request.getPathStyleAccess());
        }
        
        // Handle default configuration
        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(config.getIsDefault())) {
            // If this config is becoming default, remove default flag from other configs
            s3BucketConfigRepository.findByTenantAndIsDefaultTrue(tenant)
                    .ifPresent(existingDefault -> {
                        existingDefault.setIsDefault(false);
                        s3BucketConfigRepository.save(existingDefault);
                    });
            config.setIsDefault(true);
        }
        
        return s3BucketConfigRepository.save(config);
    }
    
    @Transactional
    public void deleteBucketConfig(UUID tenantId, UUID configId) {
        S3BucketConfig config = s3BucketConfigRepository.findById(configId)
                .filter(c -> c.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new EntityNotFoundException("Bucket configuration not found"));
        
        // Don't allow deleting the default configuration if it's the only one
        if (Boolean.TRUE.equals(config.getIsDefault()) && 
                s3BucketConfigRepository.countByTenantAndIsDefaultTrue(config.getTenant()) <= 1 &&
                s3BucketConfigRepository.findByTenant(config.getTenant()).size() <= 1) {
            throw new IllegalArgumentException("Cannot delete the only bucket configuration for a tenant");
        }
        
        // If deleting default config, make another one default
        if (Boolean.TRUE.equals(config.getIsDefault())) {
            s3BucketConfigRepository.findByTenant(config.getTenant()).stream()
                    .filter(c -> !c.getId().equals(configId))
                    .findFirst()
                    .ifPresent(newDefault -> {
                        newDefault.setIsDefault(true);
                        s3BucketConfigRepository.save(newDefault);
                    });
        }
        
        s3BucketConfigRepository.delete(config);
    }
} 