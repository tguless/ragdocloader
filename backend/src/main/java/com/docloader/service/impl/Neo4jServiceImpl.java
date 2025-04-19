package com.docloader.service.impl;

import com.docloader.model.Document;
import com.docloader.service.Neo4jService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Stub implementation of Neo4jService
 * This class will be replaced with a real implementation when Neo4j integration is implemented
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "docloader.neo4j.enabled", havingValue = "true", matchIfMissing = false)
public class Neo4jServiceImpl implements Neo4jService {

    @Override
    public String storeDocument(Document document, Map<String, Object> metadata) {
        log.info("Stub implementation: Storing document in Neo4j with ID: {}", document.getId());
        return document.getId().toString();
    }

    @Override
    public List<UUID> findRelatedDocuments(UUID documentId, String relationshipType, int limit) {
        log.info("Stub implementation: Finding related documents for ID: {} with relationship: {} and limit: {}", 
                documentId, relationshipType, limit);
        return new ArrayList<>();
    }

    @Override
    public void createRelationship(UUID sourceDocumentId, UUID targetDocumentId, String relationshipType, Map<String, Object> properties) {
        log.info("Stub implementation: Creating relationship from {} to {} of type: {}", 
                sourceDocumentId, targetDocumentId, relationshipType);
    }

    @Override
    public void removeDocument(UUID documentId) {
        log.info("Stub implementation: Removing document from Neo4j with ID: {}", documentId);
    }

    @Override
    public boolean documentExists(UUID documentId) {
        log.info("Stub implementation: Checking if document exists in Neo4j with ID: {}", documentId);
        return false;
    }
} 