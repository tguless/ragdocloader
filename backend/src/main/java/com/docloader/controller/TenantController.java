package com.docloader.controller;

import com.docloader.dto.TenantRequest;
import com.docloader.model.Role;
import com.docloader.model.Tenant;
import com.docloader.model.User;
import com.docloader.repository.RoleRepository;
import com.docloader.service.S3BucketConfigService;
import com.docloader.service.TenantService;
import com.docloader.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
@Slf4j
public class TenantController {

    private final TenantService tenantService;
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final S3BucketConfigService s3BucketConfigService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Tenant>> getAllTenants() {
        return ResponseEntity.ok(tenantService.getAllTenants());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Tenant> getTenantById(@PathVariable UUID id) {
        return tenantService.getTenantById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Tenant> createTenant(@Valid @RequestBody TenantRequest tenantRequest) {
        try {
            Tenant tenant = new Tenant();
            tenant.setName(tenantRequest.getName());
            tenant.setSubdomain(tenantRequest.getSubdomain());
            
            // Create the tenant
            Tenant savedTenant = tenantService.createTenant(tenant);
            
            // Create S3 bucket config if S3 details were provided
            if (tenantRequest.getS3BucketName() != null || tenantRequest.getS3Endpoint() != null) {
                try {
                    // The default bucket config was already created in TenantService, but we'll update it
                    s3BucketConfigService.getAllBucketConfigs(savedTenant.getId()).stream()
                            .filter(config -> Boolean.TRUE.equals(config.getIsDefault()))
                            .findFirst()
                            .ifPresent(config -> {
                                if (tenantRequest.getS3BucketName() != null) {
                                    config.setBucketName(tenantRequest.getS3BucketName());
                                }
                                if (tenantRequest.getS3Endpoint() != null) {
                                    config.setEndpoint(tenantRequest.getS3Endpoint());
                                }
                                if (tenantRequest.getS3Region() != null) {
                                    config.setRegion(tenantRequest.getS3Region());
                                }
                                if (tenantRequest.getS3AccessKey() != null) {
                                    config.setAccessKey(tenantRequest.getS3AccessKey());
                                }
                                if (tenantRequest.getS3SecretKey() != null) {
                                    config.setSecretKey(tenantRequest.getS3SecretKey());
                                }
                                if (tenantRequest.getS3PathStyleAccess() != null) {
                                    config.setPathStyleAccess(tenantRequest.getS3PathStyleAccess());
                                }
                                s3BucketConfigService.updateBucketConfig(savedTenant.getId(), config.getId(), 
                                        // Convert to S3BucketConfigRequest - would be cleaner with a proper mapper
                                        new com.docloader.dto.S3BucketConfigRequest(
                                                config.getName(), 
                                                config.getBucketName(), 
                                                config.getIsDefault(),
                                                config.getEndpoint(),
                                                config.getRegion(),
                                                config.getAccessKey(),
                                                config.getSecretKey(),
                                                config.getPathStyleAccess()
                                        ));
                            });
                } catch (Exception e) {
                    log.error("Error updating S3 bucket config: {}", e.getMessage());
                    // Don't fail tenant creation if S3 config can't be updated
                }
            }
            
            // Create the admin user for the tenant
            User adminUser = new User();
            adminUser.setUsername(tenantRequest.getAdminUsername());
            adminUser.setEmail(tenantRequest.getAdminEmail());
            adminUser.setPasswordHash(tenantRequest.getAdminPassword());
            adminUser.setTenantId(savedTenant.getId());
            
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Admin role not found"));
            adminUser.setRole(adminRole);
            
            userService.createUser(adminUser, "ADMIN");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTenant);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Error creating tenant: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating tenant");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Tenant> registerTenant(@Valid @RequestBody TenantRequest tenantRequest) {
        try {
            // This is a public endpoint to register new tenants
            // Additional validation might be needed (e.g., captcha, email verification)
            
            Tenant tenant = new Tenant();
            tenant.setName(tenantRequest.getName());
            tenant.setSubdomain(tenantRequest.getSubdomain());
            
            // Create the tenant
            Tenant savedTenant = tenantService.createTenant(tenant);
            
            // Create S3 bucket config if S3 details were provided
            if (tenantRequest.getS3BucketName() != null || tenantRequest.getS3Endpoint() != null) {
                try {
                    // The default bucket config was already created in TenantService, but we'll update it
                    s3BucketConfigService.getAllBucketConfigs(savedTenant.getId()).stream()
                            .filter(config -> Boolean.TRUE.equals(config.getIsDefault()))
                            .findFirst()
                            .ifPresent(config -> {
                                if (tenantRequest.getS3BucketName() != null) {
                                    config.setBucketName(tenantRequest.getS3BucketName());
                                }
                                if (tenantRequest.getS3Endpoint() != null) {
                                    config.setEndpoint(tenantRequest.getS3Endpoint());
                                }
                                if (tenantRequest.getS3Region() != null) {
                                    config.setRegion(tenantRequest.getS3Region());
                                }
                                if (tenantRequest.getS3AccessKey() != null) {
                                    config.setAccessKey(tenantRequest.getS3AccessKey());
                                }
                                if (tenantRequest.getS3SecretKey() != null) {
                                    config.setSecretKey(tenantRequest.getS3SecretKey());
                                }
                                if (tenantRequest.getS3PathStyleAccess() != null) {
                                    config.setPathStyleAccess(tenantRequest.getS3PathStyleAccess());
                                }
                                s3BucketConfigService.updateBucketConfig(savedTenant.getId(), config.getId(), 
                                        // Convert to S3BucketConfigRequest - would be cleaner with a proper mapper
                                        new com.docloader.dto.S3BucketConfigRequest(
                                                config.getName(), 
                                                config.getBucketName(), 
                                                config.getIsDefault(),
                                                config.getEndpoint(),
                                                config.getRegion(),
                                                config.getAccessKey(),
                                                config.getSecretKey(),
                                                config.getPathStyleAccess()
                                        ));
                            });
                } catch (Exception e) {
                    log.error("Error updating S3 bucket config: {}", e.getMessage());
                    // Don't fail tenant creation if S3 config can't be updated
                }
            }
            
            // Create the admin user for the tenant
            User adminUser = new User();
            adminUser.setUsername(tenantRequest.getAdminUsername());
            adminUser.setEmail(tenantRequest.getAdminEmail());
            adminUser.setPasswordHash(tenantRequest.getAdminPassword());
            adminUser.setTenantId(savedTenant.getId());
            
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Admin role not found"));
            adminUser.setRole(adminRole);
            
            userService.createUser(adminUser, "ADMIN");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTenant);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Error registering tenant: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error registering tenant");
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Tenant> updateTenant(@PathVariable UUID id, @Valid @RequestBody TenantRequest tenantRequest) {
        try {
            Tenant existingTenant = tenantService.getTenantById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found"));
            
            existingTenant.setName(tenantRequest.getName());
            // We don't update subdomain as that would require database migration
            
            Tenant updatedTenant = tenantService.updateTenant(id, existingTenant);
            
            // Update S3 bucket config if S3 details were provided
            if (tenantRequest.getS3BucketName() != null || tenantRequest.getS3Endpoint() != null) {
                try {
                    s3BucketConfigService.getAllBucketConfigs(id).stream()
                            .filter(config -> Boolean.TRUE.equals(config.getIsDefault()))
                            .findFirst()
                            .ifPresent(config -> {
                                if (tenantRequest.getS3BucketName() != null) {
                                    config.setBucketName(tenantRequest.getS3BucketName());
                                }
                                if (tenantRequest.getS3Endpoint() != null) {
                                    config.setEndpoint(tenantRequest.getS3Endpoint());
                                }
                                if (tenantRequest.getS3Region() != null) {
                                    config.setRegion(tenantRequest.getS3Region());
                                }
                                if (tenantRequest.getS3AccessKey() != null) {
                                    config.setAccessKey(tenantRequest.getS3AccessKey());
                                }
                                if (tenantRequest.getS3SecretKey() != null) {
                                    config.setSecretKey(tenantRequest.getS3SecretKey());
                                }
                                if (tenantRequest.getS3PathStyleAccess() != null) {
                                    config.setPathStyleAccess(tenantRequest.getS3PathStyleAccess());
                                }
                                s3BucketConfigService.updateBucketConfig(id, config.getId(), 
                                        // Convert to S3BucketConfigRequest - would be cleaner with a proper mapper
                                        new com.docloader.dto.S3BucketConfigRequest(
                                                config.getName(), 
                                                config.getBucketName(), 
                                                config.getIsDefault(),
                                                config.getEndpoint(),
                                                config.getRegion(),
                                                config.getAccessKey(),
                                                config.getSecretKey(),
                                                config.getPathStyleAccess()
                                        ));
                            });
                } catch (Exception e) {
                    log.error("Error updating S3 bucket config: {}", e.getMessage());
                    // Don't fail tenant update if S3 config can't be updated
                }
            }
            
            return ResponseEntity.ok(updatedTenant);
        } catch (Exception e) {
            log.error("Error updating tenant: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating tenant");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTenant(@PathVariable UUID id) {
        try {
            tenantService.deleteTenant(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting tenant: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting tenant");
        }
    }
} 