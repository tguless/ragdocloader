package com.docloader.controller;

import com.docloader.dto.AuthRequest;
import com.docloader.dto.AuthResponse;
import com.docloader.dto.RegisterRequest;
import com.docloader.model.Role;
import com.docloader.model.Tenant;
import com.docloader.model.User;
import com.docloader.repository.RoleRepository;
import com.docloader.security.UserDetailsImpl;
import com.docloader.service.AuthService;
import com.docloader.service.TenantService;
import com.docloader.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final TenantService tenantService;
    private final RoleRepository roleRepository;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody AuthRequest loginRequest) {
        try {
            log.info("Login attempt for user: {}", loginRequest.getUsername());
            String jwt = authService.authenticateUser(loginRequest.getUsername(), loginRequest.getPassword());
            UserDetailsImpl userDetails = authService.getCurrentUser();
            
            log.info("Authentication successful for user: {}", loginRequest.getUsername());
            
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            
            AuthResponse response = new AuthResponse(
                    jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    userDetails.getTenantId(),
                    roles
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Authentication failed for user {}: {}", loginRequest.getUsername(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password", e);
        }
    }

    // Handle OPTIONS preflight requests for CORS
    @RequestMapping(value = "/login", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> preflightLogin() {
        log.info("Received preflight OPTIONS request for /login endpoint");
        return ResponseEntity
            .ok()
            .build();
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // Check if tenant exists
            Tenant tenant = tenantService.getTenantBySubdomain(registerRequest.getTenantSubdomain())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found"));
            
            // Create user
            Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Default role not found"));
            
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setEmail(registerRequest.getEmail());
            user.setPasswordHash(registerRequest.getPassword());
            user.setTenantId(tenant.getId());
            user.setRole(userRole);
            
            User savedUser = userService.createUser(user, "USER");
            
            // Login the user
            String jwt = authService.authenticateUser(registerRequest.getUsername(), registerRequest.getPassword());
            
            AuthResponse response = new AuthResponse(
                    jwt,
                    savedUser.getId(),
                    savedUser.getUsername(),
                    savedUser.getEmail(),
                    savedUser.getTenantId(),
                    List.of(savedUser.getRole().getName())
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserDetailsImpl> getCurrentUser() {
        UserDetailsImpl currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return ResponseEntity.ok(currentUser);
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        log.info("Received ping request");
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "Auth service is up and running");
        return ResponseEntity.ok(response);
    }

    // Simple test endpoint that just returns success
    @PostMapping("/raw-login")
    public ResponseEntity<Map<String, Object>> rawLogin(
            HttpServletRequest request) {
        log.info("Raw login endpoint accessed");
        
        try {
            // Try to read the request body directly
            StringBuilder buffer = new StringBuilder();
            String line;
            java.io.BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            String payload = buffer.toString();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Raw login endpoint working correctly");
            response.put("timestamp", System.currentTimeMillis());
            response.put("raw_payload", payload);
            
            // Try to parse manually
            if (payload != null && !payload.isEmpty()) {
                response.put("payload_length", payload.length());
                if (payload.contains("username") && payload.contains("password")) {
                    response.put("contains_credentials", true);
                }
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in raw login: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    // Login with path variables
    @GetMapping("/path-login/{username}/{password}")
    public ResponseEntity<Map<String, Object>> pathLogin(
            @PathVariable("username") String username,
            @PathVariable("password") String password) {
        log.info("Path login endpoint accessed for user: {}", username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Path login endpoint working correctly");
        response.put("timestamp", System.currentTimeMillis());
        response.put("username", username);
        response.put("password_length", password.length());
        
        return ResponseEntity.ok(response);
    }
} 