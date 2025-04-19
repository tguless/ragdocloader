package com.docloader.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tenants", schema = "app")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String subdomain;

    @Column(name = "db_name", nullable = false, unique = true)
    private String dbName;
    
    // S3 bucket configurations
    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<S3BucketConfig> bucketConfigs = new ArrayList<>();

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // Helper methods for managing bucket configurations
    public void addBucketConfig(S3BucketConfig bucketConfig) {
        bucketConfigs.add(bucketConfig);
        bucketConfig.setTenant(this);
    }
    
    public void removeBucketConfig(S3BucketConfig bucketConfig) {
        bucketConfigs.remove(bucketConfig);
        bucketConfig.setTenant(null);
    }
    
    public S3BucketConfig getDefaultBucketConfig() {
        return bucketConfigs.stream()
                .filter(config -> Boolean.TRUE.equals(config.getIsDefault()))
                .findFirst()
                .orElse(bucketConfigs.isEmpty() ? null : bucketConfigs.get(0));
    }
    
    public S3BucketConfig getBucketConfigById(UUID id) {
        return bucketConfigs.stream()
                .filter(config -> config.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
    
    public S3BucketConfig getBucketConfigByName(String name) {
        return bucketConfigs.stream()
                .filter(config -> config.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
} 