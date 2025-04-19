package com.docloader.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "s3_bucket_configs", schema = "app")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class S3BucketConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, name = "bucket_name")
    private String bucketName;
    
    @Column(name = "is_default")
    private Boolean isDefault = false;
    
    @Column(name = "endpoint")
    private String endpoint;
    
    @Column(name = "region")
    private String region;
    
    @Column(name = "access_key")
    private String accessKey;
    
    @Column(name = "secret_key")
    private String secretKey;
    
    @Column(name = "path_style_access")
    private Boolean pathStyleAccess = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
} 