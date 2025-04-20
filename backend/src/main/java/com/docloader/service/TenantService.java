package com.docloader.service;

import com.docloader.model.Tenant;
import com.docloader.repository.TenantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class TenantService {

    private final TenantRepository tenantRepository;
    private final JdbcTemplate jdbcTemplate;
    private final S3BucketConfigService s3BucketConfigService;
    
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
    public TenantService(
            TenantRepository tenantRepository,
            JdbcTemplate jdbcTemplate,
            @Lazy S3BucketConfigService s3BucketConfigService,
            @Qualifier("s3BucketName") String defaultBucketName) {
        this.tenantRepository = tenantRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.s3BucketConfigService = s3BucketConfigService;
        this.defaultBucketName = defaultBucketName;
    }

    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    public Optional<Tenant> getTenantById(UUID id) {
        return tenantRepository.findById(id);
    }

    public Optional<Tenant> getTenantBySubdomain(String subdomain) {
        return tenantRepository.findBySubdomain(subdomain);
    }

    @Transactional
    public Tenant createTenant(Tenant tenant) {
        log.info("Creating new tenant: {}", tenant.getName());
        
        // Validate tenant data
        if (tenantRepository.existsBySubdomain(tenant.getSubdomain())) {
            throw new IllegalArgumentException("Subdomain already exists: " + tenant.getSubdomain());
        }
        
        // Create schema name from tenant ID (will be created after the tenant is saved)
        // Will store the schema name with underscores instead of hyphens to be PostgreSQL compatible
        String uuidStr = UUID.randomUUID().toString().replace("-", "_");
        tenant.setDbName("tenant_" + uuidStr);
        
        // Save tenant in the main database
        Tenant savedTenant = tenantRepository.save(tenant);
        
        // Create the tenant schema using the PostgreSQL function
        createTenantSchema(savedTenant.getName(), savedTenant.getId().toString().replace("-", "_"));
        
        // Create S3 bucket configuration for the tenant
        try {
            s3BucketConfigService.createDefaultBucketConfig(savedTenant);
            log.info("Successfully created S3 bucket config for tenant: {}", savedTenant.getName());
        } catch (Exception e) {
            log.error("Failed to create S3 bucket config for tenant '{}': {}", 
                    savedTenant.getName(), e.getMessage(), e);
            // Don't fail the tenant creation if bucket creation fails
        }
        
        return savedTenant;
    }

    @Transactional
    public Tenant updateTenant(UUID id, Tenant tenantDetails) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found with id: " + id));
        
        tenant.setName(tenantDetails.getName());
        // Note: We don't allow changing subdomain or dbName as that would require database migration
        
        return tenantRepository.save(tenant);
    }

    @Transactional
    public void deleteTenant(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found with id: " + id));
        
        // We should consider implementing a soft delete instead, and archive tenant data
        tenantRepository.delete(tenant);
        
        // Optionally drop the tenant schema, but this is dangerous and should have safeguards
        // dropTenantSchema(tenant.getDbName());
    }
    
    private UUID createTenantSchema(String name, String schemaId) {
        // Use the PostgreSQL function to create the tenant schema
        return jdbcTemplate.queryForObject(
                "SELECT app.create_tenant_database(?, ?)", 
                UUID.class, 
                name, 
                schemaId);
    }
    
    // This is dangerous and should be used with caution - consider not implementing this at all
    private void dropTenantSchema(String schemaName) {
        // Implement additional safety checks before allowing this operation
        log.warn("Request to drop tenant schema: {}", schemaName);
        // jdbcTemplate.execute("DROP SCHEMA IF EXISTS " + schemaName + " CASCADE");
    }
} 