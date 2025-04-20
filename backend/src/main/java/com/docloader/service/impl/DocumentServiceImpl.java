package com.docloader.service.impl;

import com.docloader.dto.DocumentResponse;
import com.docloader.model.Document;
import com.docloader.repository.DocumentRepository;
import com.docloader.service.DocumentService;
import com.docloader.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final S3Service s3Service;
    
    @Value("${docloader.uploads.dir:uploads}")
    private String uploadsDir;
    
    @Value("${docloader.storage.type:filesystem}")
    private String storageType;

    @Override
    public Optional<Document> getDocumentById(UUID id) {
        return documentRepository.findById(id);
    }

    @Override
    public List<Document> getDocumentsByJobId(UUID jobId) {
        return documentRepository.findByJobId(jobId);
    }
    
    @Override
    public List<Document> getDocumentsByUploadedBy(UUID uploadedBy) {
        return documentRepository.findByUploadedBy(uploadedBy);
    }

    @Override
    @Transactional
    public Document saveDocument(Document document) {
        return documentRepository.save(document);
    }

    @Override
    @Transactional
    public void deleteDocument(UUID id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Document not found with id: " + id));
        
        try {
            // Delete the physical file
            if ("s3".equalsIgnoreCase(storageType)) {
                s3Service.deleteFile(document.getFilePath());
            } else {
                Path filePath = Paths.get(document.getFilePath());
                Files.deleteIfExists(filePath);
            }
            
            // Delete the database record
            documentRepository.deleteById(id);
            
        } catch (IOException e) {
            log.error("Error deleting file {}: {}", document.getFilePath(), e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Failed to delete file");
        }
    }

    @Override
    public int getDocumentCountByUserId(UUID userId) {
        // For now, we'll return a default value
        // In a real implementation, you would query the repository with user ID
        return 0;
    }
    
    @Override
    @Transactional
    public List<Document> uploadDocuments(List<MultipartFile> files, UUID uploadedBy, UUID tenantId) {
        List<Document> documents = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                String filename = StringUtils.cleanPath(file.getOriginalFilename());
                
                if (filename.contains("..")) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                            "Filename contains invalid path sequence: " + filename);
                }
                
                // Calculate MD5 hash
                String md5Hash = calculateMD5(file);
                
                // Create unique filepath 
                String uniqueFilename = UUID.randomUUID().toString() + "_" + filename;
                String tenantFolder = tenantId.toString();
                String userFolder = uploadedBy.toString();
                String filePath;
                
                if ("s3".equalsIgnoreCase(storageType)) {
                    // Store in S3
                    filePath = String.format("uploads/%s/%s/%s", tenantFolder, userFolder, uniqueFilename);
                    s3Service.uploadFile(filePath, file.getInputStream(), file.getSize(), file.getContentType());
                } else {
                    // Store in filesystem
                    Path uploadPath = Paths.get(uploadsDir, tenantFolder, userFolder);
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }
                    
                    Path destinationFile = uploadPath.resolve(uniqueFilename);
                    Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
                    filePath = destinationFile.toString();
                }
                
                Document document = new Document();
                document.setFilename(filename);
                document.setFilePath(filePath);
                document.setFileSize(file.getSize());
                document.setContentType(file.getContentType());
                document.setMd5Hash(md5Hash);
                document.setUploadedBy(uploadedBy);
                document.setStatus(Document.DocumentStatus.PENDING);
                
                documents.add(documentRepository.save(document));
                
            } catch (IOException | NoSuchAlgorithmException e) {
                log.error("Error uploading file {}: {}", file.getOriginalFilename(), e.getMessage(), e);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                        "Failed to store file: " + file.getOriginalFilename());
            }
        }
        
        return documents;
    }
    
    @Override
    public DocumentResponse toDocumentResponse(Document document) {
        DocumentResponse response = new DocumentResponse();
        response.setId(document.getId());
        response.setJobId(document.getJob() != null ? document.getJob().getId() : null);
        response.setFilename(document.getFilename());
        response.setContentType(document.getContentType());
        response.setFileSize(document.getFileSize());
        response.setStatus(document.getStatus());
        response.setUploadedBy(document.getUploadedBy());
        response.setCreatedAt(document.getCreatedAt());
        response.setProcessedAt(document.getProcessedAt());
        return response;
    }
    
    private String calculateMD5(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(file.getBytes());
        byte[] digest = md.digest();
        
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
} 