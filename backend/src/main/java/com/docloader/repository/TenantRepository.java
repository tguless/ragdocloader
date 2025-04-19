package com.docloader.repository;

import com.docloader.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findBySubdomain(String subdomain);
    
    boolean existsBySubdomain(String subdomain);
    
    Optional<Tenant> findByDbName(String dbName);
} 