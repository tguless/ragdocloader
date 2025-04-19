package com.docloader.repository;

import com.docloader.model.S3BucketConfig;
import com.docloader.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface S3BucketConfigRepository extends JpaRepository<S3BucketConfig, UUID> {
    
    List<S3BucketConfig> findByTenant(Tenant tenant);
    
    List<S3BucketConfig> findByTenantId(UUID tenantId);
    
    Optional<S3BucketConfig> findByTenantAndIsDefaultTrue(Tenant tenant);
    
    Optional<S3BucketConfig> findByTenantAndName(Tenant tenant, String name);
    
    boolean existsByTenantAndName(Tenant tenant, String name);
    
    boolean existsByTenantAndBucketName(Tenant tenant, String bucketName);
    
    long countByTenantAndIsDefaultTrue(Tenant tenant);
} 