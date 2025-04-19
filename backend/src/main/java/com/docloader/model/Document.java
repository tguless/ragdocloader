package com.docloader.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "documents", schema = "tenant_template", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"file_path", "md5_hash"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private DocumentJob job;

    @Column(nullable = false)
    private String filename;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "processed_at")
    @CreationTimestamp
    private LocalDateTime processedAt;

    @Column(name = "embedding_vector_id")
    private String embeddingVectorId;

    @Column
    @Enumerated(EnumType.STRING)
    private DocumentStatus status = DocumentStatus.PROCESSED;

    @Column(name = "md5_hash", nullable = false)
    private String md5Hash;
    
    public enum DocumentStatus {
        PROCESSED, FAILED, SKIPPED
    }
} 