package com.docloader.service;

import com.docloader.model.Role;
import com.docloader.model.User;
import com.docloader.repository.RoleRepository;
import com.docloader.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public Optional<User> getUserByUsernameAndTenantId(String username, UUID tenantId) {
        return userRepository.findByUsernameAndTenantId(username, tenantId);
    }

    @Transactional
    public User createUser(User user, String roleName) {
        log.info("Creating new user: {} for tenant: {}", user.getUsername(), user.getTenantId());
        
        // Validate user data
        if (userRepository.existsByUsernameAndTenantId(user.getUsername(), user.getTenantId())) {
            throw new IllegalArgumentException("Username already exists in this tenant: " + user.getUsername());
        }
        
        if (userRepository.existsByEmailAndTenantId(user.getEmail(), user.getTenantId())) {
            throw new IllegalArgumentException("Email already exists in this tenant: " + user.getEmail());
        }
        
        // Find and set the role
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleName));
        user.setRole(role);
        
        // Encode the password
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        
        return userRepository.save(user);
    }
    
    @Transactional
    public User createAdminUser(User user) {
        return createUser(user, "ADMIN");
    }
    
    @Transactional
    public User createRegularUser(User user) {
        return createUser(user, "USER");
    }

    @Transactional
    public User updateUser(UUID id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        
        // Update basic info
        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
        
        // Update password if provided
        if (userDetails.getPasswordHash() != null && !userDetails.getPasswordHash().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(userDetails.getPasswordHash()));
        }
        
        // Update role if provided
        if (userDetails.getRole() != null) {
            Role role = roleRepository.findById(userDetails.getRole().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + userDetails.getRole().getId()));
            user.setRole(role);
        }
        
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        
        userRepository.delete(user);
    }
    
    @Transactional
    public boolean changePassword(UUID userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            return false;
        }
        
        // Set new password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return true;
    }
} 