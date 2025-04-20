package com.docloader.service;

import com.docloader.model.Document;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentService {
    
    /**
     * Get a document by its ID
     * @param id the document ID
     * @return the document if found
     */
    Optional<Document> getDocumentById(UUID id);
    
    /**
     * Get all documents for a job
     * @param jobId the job ID
     * @return list of documents
     */
    List<Document> getDocumentsByJobId(UUID jobId);
    
    /**
     * Save a document
     * @param document the document to save
     * @return the saved document
     */
    Document saveDocument(Document document);
    
    /**
     * Delete a document
     * @param id the document ID
     */
    void deleteDocument(UUID id);
    
    /**
     * Get the count of documents for a user
     * @param userId the user ID
     * @return the count of documents
     */
    int getDocumentCountByUserId(UUID userId);
} 