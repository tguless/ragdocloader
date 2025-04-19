package com.docloader.service;

import com.docloader.model.Document;
import com.docloader.model.DocumentJob;
import com.docloader.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;  

@Service
@Slf4j
public class DocumentProcessingService {

    private final DocumentJobService documentJobService;
    private final DocumentRepository documentRepository;
    private final EmbeddingModel embeddingModel;
    private final S3Service s3Service;
    
    @Autowired(required = false)
    private WeaviateService weaviateService;
    
    @Autowired(required = false)
    private Neo4jService neo4jService;

    public DocumentProcessingService(
            DocumentJobService documentJobService,
            DocumentRepository documentRepository,
            EmbeddingModel embeddingModel,
            S3Service s3Service) {
        this.documentJobService = documentJobService;
        this.documentRepository = documentRepository;
        this.embeddingModel = embeddingModel;
        this.s3Service = s3Service;
    }

    @Async
    public CompletableFuture<Void> processJobAsync(UUID jobId) {
        return CompletableFuture.runAsync(() -> {
            try {
                processJob(jobId);
            } catch (Exception e) {
                log.error("Error processing job {}: {}", jobId, e.getMessage(), e);
                // Update job status to FAILED
                documentJobService.updateJobStatus(jobId, DocumentJob.JobStatus.FAILED);
            }
        });
    }

    @Transactional
    public void processJob(UUID jobId) {
        log.info("Processing document job: {}", jobId);
        
        // Get the job
        DocumentJob job = documentJobService.getJobById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
        
        try {
            // TODO: Implement actual document processing logic
            // 1. Retrieve documents from S3 (or other source)
            // 2. Check if documents are already processed (using MD5 hash)
            // 3. Process documents and extract text
            // 4. Generate embeddings
            // 5. Store in vector database
            // 6. Build relationships in Neo4j
            
            // PLACEHOLDER IMPLEMENTATION
            log.info("Document job processing completed: {}", jobId);
            
            // Update job status to COMPLETED
            documentJobService.updateJobStatus(jobId, DocumentJob.JobStatus.COMPLETED);
        } catch (Exception e) {
            log.error("Error processing job {}: {}", jobId, e.getMessage(), e);
            // Update job status to FAILED
            documentJobService.updateJobStatus(jobId, DocumentJob.JobStatus.FAILED);
            throw e;
        }
    }
} 