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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
            String jwt = authService.authenticateUser(loginRequest.getUsername(), loginRequest.getPassword());
            UserDetailsImpl userDetails = authService.getCurrentUser();
            
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
            log.error("Authentication failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password", e);
        }
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
} 