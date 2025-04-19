package com.docloader.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    
    private String token;
    private UUID id;
    private String username;
    private String email;
    private UUID tenantId;
    private List<String> roles;
} 