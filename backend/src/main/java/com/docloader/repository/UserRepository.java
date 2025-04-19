package com.docloader.repository;

import com.docloader.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    
    Optional<User> findByUsernameAndTenantId(String username, UUID tenantId);
    
    Optional<User> findByEmailAndTenantId(String email, UUID tenantId);
    
    boolean existsByUsernameAndTenantId(String username, UUID tenantId);
    
    boolean existsByEmailAndTenantId(String email, UUID tenantId);
} 