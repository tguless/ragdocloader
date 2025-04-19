package com.docloader.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TenantRequest {
    
    @NotBlank
    @Size(min = 3, max = 100)
    private String name;
    
    @NotBlank
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Subdomain can only contain lowercase letters, numbers, and hyphens")
    private String subdomain;
    
    @NotBlank
    @Size(min = 3, max = 50)
    private String adminUsername;
    
    @NotBlank
    @Size(min = 6, max = 40)
    private String adminPassword;
    
    @NotBlank
    @Size(max = 100)
    @Email
    private String adminEmail;
} 