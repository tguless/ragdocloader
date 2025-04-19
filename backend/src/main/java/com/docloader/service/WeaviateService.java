package com.docloader.service;

import com.docloader.model.Document;

import java.util.List;
import java.util.UUID;

/**
 * Service for interacting with Weaviate vector database to store and retrieve document embeddings
 */
public interface WeaviateService {

    /**
     * Store document embeddings in Weaviate
     * @param document the document entity
     * @param embedding the document's embedding vector
     * @return the document ID in Weaviate
     */
    String storeDocumentEmbedding(Document document, float[] embedding);
    
    /**
     * Find similar documents based on embedding vector
     * @param embedding the query embedding vector
     * @param limit the maximum number of results to return
     * @return list of document IDs with similarity scores
     */
    List<String> findSimilarDocuments(float[] embedding, int limit);
    
    /**
     * Remove a document from Weaviate
     * @param documentId the document ID to remove
     */
    void removeDocument(UUID documentId);
    
    /**
     * Check if the document is already stored in Weaviate
     * @param documentId the document ID to check
     * @return true if the document exists, false otherwise
     */
    boolean documentExists(UUID documentId);
} 