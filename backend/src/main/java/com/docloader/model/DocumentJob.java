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
@Table(name = "document_jobs", schema = "app")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "source_location", nullable = false)
    private String sourceLocation;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private JobStatus status = JobStatus.PENDING;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Column(name = "completed_time")
    private LocalDateTime completedTime;
    
    public enum JobStatus {
        PENDING, PROCESSING, COMPLETED, FAILED, SCHEDULED
    }
} 