package com.docloader.service;

import com.docloader.model.User;
import com.docloader.multitenancy.TenantContext;
import com.docloader.security.JwtUtils;
import com.docloader.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;

    public String authenticateUser(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtUtils.generateJwtToken(authentication);
    }

    public UserDetailsImpl getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            return (UserDetailsImpl) authentication.getPrincipal();
        }
        return null;
    }

    public UUID getCurrentUserId() {
        UserDetailsImpl userDetails = getCurrentUser();
        return userDetails != null ? userDetails.getId() : null;
    }

    public String getCurrentUsername() {
        UserDetailsImpl userDetails = getCurrentUser();
        return userDetails != null ? userDetails.getUsername() : null;
    }

    public UUID getCurrentTenantId() {
        UserDetailsImpl userDetails = getCurrentUser();
        return userDetails != null ? userDetails.getTenantId() : null;
    }

    public User getCurrentUserEntity() {
        UserDetailsImpl userDetails = getCurrentUser();
        if (userDetails == null) {
            return null;
        }
        
        // Set tenant context for database operations
        if (userDetails.getTenantId() != null) {
            String dbName = "tenant_" + userDetails.getTenantId();
            TenantContext.setCurrentTenant(dbName);
        }
        
        return userService.getUserById(userDetails.getId()).orElse(null);
    }
} 