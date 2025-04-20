package com.docloader.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service class for handling security-related authorization checks.
 * This is referenced in PreAuthorize annotations throughout the application.
 */
@Service
public class SecurityService {

    /**
     * Checks if the current user is an admin for the specified tenant or a system admin.
     * 
     * @param tenantId The tenant ID to check against
     * @return true if the user has tenant admin or system admin privileges
     */
    public boolean isTenantAdmin(UUID tenantId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        
        // Check if user has ROLE_TENANT_ADMIN or ROLE_SYSTEM_ADMIN
        return auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_TENANT_ADMIN") || 
                           a.getAuthority().equals("ROLE_SYSTEM_ADMIN"));
    }
    
    /**
     * Checks if the current user is a system administrator.
     * 
     * @return true if the user has system admin privileges
     */
    public boolean isSystemAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        
        // Check if user has ROLE_SYSTEM_ADMIN
        return auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_SYSTEM_ADMIN"));
    }
} 