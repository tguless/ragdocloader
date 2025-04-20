package com.docloader.service;

import com.docloader.dto.DocumentResponse;
import com.docloader.model.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentService {
    
    Optional<Document> getDocumentById(UUID id);
    
    List<Document> getDocumentsByJobId(UUID jobId);
    
    List<Document> getDocumentsByUploadedBy(UUID uploadedBy);
    
    List<Document> uploadDocuments(List<MultipartFile> files, UUID uploadedBy, UUID tenantId);
    
    DocumentResponse toDocumentResponse(Document document);
    
    Document saveDocument(Document document);
    
    void deleteDocument(UUID id);
    
    int getDocumentCountByUserId(UUID userId);
}