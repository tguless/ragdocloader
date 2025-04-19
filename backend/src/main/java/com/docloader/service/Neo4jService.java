package com.docloader.service;

import com.docloader.model.Document;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for interacting with Neo4j graph database to store and retrieve document relationships
 */
public interface Neo4jService {

    /**
     * Store document metadata and relationships in Neo4j
     * @param document the document entity
     * @param metadata additional metadata to store with the document
     * @return the document ID in Neo4j
     */
    String storeDocument(Document document, Map<String, Object> metadata);
    
    /**
     * Find related documents based on document ID
     * @param documentId the document ID to find related documents for
     * @param relationshipType the type of relationship to follow
     * @param limit the maximum number of results to return
     * @return list of related document IDs
     */
    List<UUID> findRelatedDocuments(UUID documentId, String relationshipType, int limit);
    
    /**
     * Create a relationship between two documents
     * @param sourceDocumentId the source document ID
     * @param targetDocumentId the target document ID
     * @param relationshipType the type of relationship
     * @param properties optional properties to add to the relationship
     */
    void createRelationship(UUID sourceDocumentId, UUID targetDocumentId, String relationshipType, Map<String, Object> properties);
    
    /**
     * Remove a document from Neo4j
     * @param documentId the document ID to remove
     */
    void removeDocument(UUID documentId);
    
    /**
     * Check if the document is already stored in Neo4j
     * @param documentId the document ID to check
     * @return true if the document exists, false otherwise
     */
    boolean documentExists(UUID documentId);
} 