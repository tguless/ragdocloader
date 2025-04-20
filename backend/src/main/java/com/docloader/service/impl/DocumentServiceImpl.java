package com.docloader.service.impl;

import com.docloader.model.Document;
import com.docloader.repository.DocumentRepository;
import com.docloader.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;

    @Override
    public Optional<Document> getDocumentById(UUID id) {
        return documentRepository.findById(id);
    }

    @Override
    public List<Document> getDocumentsByJobId(UUID jobId) {
        return documentRepository.findByJobId(jobId);
    }

    @Override
    @Transactional
    public Document saveDocument(Document document) {
        return documentRepository.save(document);
    }

    @Override
    @Transactional
    public void deleteDocument(UUID id) {
        documentRepository.deleteById(id);
    }

    @Override
    public int getDocumentCountByUserId(UUID userId) {
        // For now, we'll return a default value
        // In a real implementation, you would query the repository with user ID
        return 0;
    }
} 