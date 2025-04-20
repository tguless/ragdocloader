package com.docloader.security;

import com.docloader.model.User;
import com.docloader.model.Tenant;
import com.docloader.multitenancy.TenantContext;
import com.docloader.repository.UserRepository;
import com.docloader.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Attempting to load user by username: {}", username);
        
        // Check if tenant context is available
        String tenantId = TenantContext.getCurrentTenant();
        User user = null;
        
        if (tenantId != null) {
            // Normal flow with tenant context
            log.info("Loading user with tenant context: {}", tenantId);
            try {
                Optional<User> userOpt = userRepository.findByUsername(username);
                if (userOpt.isPresent()) {
                    user = userOpt.get();
                    log.info("User found in tenant {}: {} (ID: {})", tenantId, user.getUsername(), user.getId());
                } else {
                    log.warn("User '{}' not found in tenant: {}", username, tenantId);
                    throw new UsernameNotFoundException("User Not Found with username: " + username + " in tenant: " + tenantId);
                }
            } catch (Exception e) {
                log.error("Error finding user '{}' in tenant {}: {}", username, tenantId, e.getMessage(), e);
                throw new UsernameNotFoundException("Error finding user: " + e.getMessage());
            }
        } else {
            // For auth endpoints, we might not have tenant context yet
            // Try to find the user across all tenants
            log.info("No tenant context available, searching for user '{}' across all tenants", username);
            
            // Get all tenants
            List<Tenant> tenants;
            try {
                tenants = tenantRepository.findAll();
                log.info("Found {} tenants to search", tenants.size());
            } catch (Exception e) {
                log.error("Error fetching tenants: {}", e.getMessage(), e);
                throw new UsernameNotFoundException("Could not load tenants: " + e.getMessage());
            }
            
            // Try each tenant until we find the user
            for (Tenant tenant : tenants) {
                String dbName = "tenant_" + tenant.getId();
                log.debug("Setting tenant context to: {}", dbName);
                TenantContext.setCurrentTenant(dbName);
                
                try {
                    Optional<User> foundUser = userRepository.findByUsername(username);
                    if (foundUser.isPresent()) {
                        user = foundUser.get();
                        log.info("Found user '{}' (ID: {}) in tenant: {}", username, user.getId(), dbName);
                        break;
                    } else {
                        log.debug("User '{}' not found in tenant: {}", username, dbName);
                    }
                } catch (Exception e) {
                    log.warn("Error searching user '{}' in tenant {}: {}", username, dbName, e.getMessage());
                }
            }
            
            // Reset tenant context if we couldn't find the user
            if (user == null) {
                log.error("User '{}' not found in any tenant", username);
                TenantContext.clear();
                throw new UsernameNotFoundException("User Not Found with username: " + username);
            }
        }

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        log.info("Built UserDetails for user: {} with authorities: {}", user.getUsername(), 
                userDetails.getAuthorities().stream().map(auth -> auth.getAuthority()).toList());
        return userDetails;
    }
} 