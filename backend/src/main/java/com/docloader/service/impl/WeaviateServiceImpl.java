package com.docloader.service.impl;

import com.docloader.model.Document;
import com.docloader.service.WeaviateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Stub implementation of WeaviateService
 * This class will be replaced with a real implementation when Weaviate integration is implemented
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "docloader.weaviate.enabled", havingValue = "true", matchIfMissing = false)
public class WeaviateServiceImpl implements WeaviateService {

    @Override
    public String storeDocumentEmbedding(Document document, float[] embedding) {
        log.info("Stub implementation: Storing document embedding for document ID: {}", document.getId());
        return document.getId().toString();
    }

    @Override
    public List<String> findSimilarDocuments(float[] embedding, int limit) {
        log.info("Stub implementation: Finding similar documents with limit: {}", limit);
        return new ArrayList<>();
    }

    @Override
    public void removeDocument(UUID documentId) {
        log.info("Stub implementation: Removing document ID: {}", documentId);
    }

    @Override
    public boolean documentExists(UUID documentId) {
        log.info("Stub implementation: Checking if document exists with ID: {}", documentId);
        return false;
    }
} 