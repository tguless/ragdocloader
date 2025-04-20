package com.docloader.controller;

import com.docloader.dto.DocumentResponse;
import com.docloader.model.Document;
import com.docloader.service.AuthService;
import com.docloader.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Documents", description = "API endpoints for managing documents")
public class DocumentController {

    private final DocumentService documentService;
    private final AuthService authService;

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SYSTEM_ADMIN')")
    @Operation(summary = "Upload documents")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Map<String, List<UUID>>> uploadDocuments(@RequestParam("files") List<MultipartFile> files) {
        try {
            log.info("Uploading {} documents", files.size());
            
            if (files.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No files provided");
            }
            
            UUID currentUserId = authService.getCurrentUserId();
            UUID tenantId = authService.getCurrentUser().getTenantId();
            
            List<Document> documents = documentService.uploadDocuments(files, currentUserId, tenantId);
            
            List<UUID> documentIds = documents.stream()
                    .map(Document::getId)
                    .collect(Collectors.toList());
            
            log.info("Successfully uploaded {} documents", documentIds.size());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("documentIds", documentIds));
            
        } catch (Exception e) {
            log.error("Error uploading documents: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error uploading documents");
        }
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SYSTEM_ADMIN')")
    @Operation(summary = "Get user documents")
    public ResponseEntity<List<DocumentResponse>> getUserDocuments() {
        try {
            UUID currentUserId = authService.getCurrentUserId();
            log.info("Getting documents for user ID: {}", currentUserId);
            
            List<Document> documents = documentService.getDocumentsByUploadedBy(currentUserId);
            
            List<DocumentResponse> responses = documents.stream()
                    .map(documentService::toDocumentResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting user documents: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving documents");
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SYSTEM_ADMIN')")
    @Operation(summary = "Get document by ID")
    public ResponseEntity<DocumentResponse> getDocumentById(@PathVariable UUID id) {
        try {
            log.info("Getting document by ID: {}", id);
            
            Document document = documentService.getDocumentById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
            
            // Check access permissions
            UUID currentUserId = authService.getCurrentUserId();
            if (!currentUserId.equals(document.getUploadedBy()) && 
                authService.getCurrentUser().getAuthorities().stream()
                    .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SYSTEM_ADMIN"))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to this document");
            }
            
            return ResponseEntity.ok(documentService.toDocumentResponse(document));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting document by ID {}: {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving document");
        }
    }
    
    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SYSTEM_ADMIN')")
    @Operation(summary = "Get documents for a job")
    public ResponseEntity<List<DocumentResponse>> getDocumentsByJobId(@PathVariable UUID jobId) {
        try {
            log.info("Getting documents for job ID: {}", jobId);
            
            List<Document> documents = documentService.getDocumentsByJobId(jobId);
            
            List<DocumentResponse> responses = documents.stream()
                    .map(documentService::toDocumentResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting documents for job ID {}: {}", jobId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving documents");
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SYSTEM_ADMIN')")
    @Operation(summary = "Delete document")
    public ResponseEntity<Void> deleteDocument(@PathVariable UUID id) {
        try {
            log.info("Deleting document with ID: {}", id);
            
            Document document = documentService.getDocumentById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
            
            // Check access permissions
            UUID currentUserId = authService.getCurrentUserId();
            if (!currentUserId.equals(document.getUploadedBy()) && 
                authService.getCurrentUser().getAuthorities().stream()
                    .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SYSTEM_ADMIN"))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to delete this document");
            }
            
            // Don't allow deletion if document is associated with a job
            if (document.getJob() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Cannot delete document associated with a job. Delete the job first.");
            }
            
            documentService.deleteDocument(id);
            
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting document with ID {}: {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting document");
        }
    }
} 