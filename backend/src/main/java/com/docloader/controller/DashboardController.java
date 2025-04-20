package com.docloader.controller;

import com.docloader.model.Document;
import com.docloader.model.DocumentJob;
import com.docloader.service.AuthService;
import com.docloader.service.DocumentJobService;
import com.docloader.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DocumentJobService documentJobService;
    private final DocumentService documentService;
    private final AuthService authService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        UUID userId = authService.getCurrentUserId();
        
        // Get all jobs for the current user
        List<DocumentJob> jobs = documentJobService.getAllJobsByUserId(userId);
        
        // Calculate statistics
        int totalJobs = jobs.size();
        int pendingJobs = 0;
        int completedJobs = 0;
        int failedJobs = 0;
        
        for (DocumentJob job : jobs) {
            if (job.getStatus() == DocumentJob.JobStatus.PENDING) {
                pendingJobs++;
            } else if (job.getStatus() == DocumentJob.JobStatus.COMPLETED) {
                completedJobs++;
            } else if (job.getStatus() == DocumentJob.JobStatus.FAILED) {
                failedJobs++;
            }
        }
        
        // Get total documents
        int totalDocuments = documentService.getDocumentCountByUserId(userId);
        
        // Build response
        Map<String, Object> dashboardStats = new HashMap<>();
        dashboardStats.put("totalJobs", totalJobs);
        dashboardStats.put("pendingJobs", pendingJobs);
        dashboardStats.put("completedJobs", completedJobs);
        dashboardStats.put("failedJobs", failedJobs);
        dashboardStats.put("totalDocuments", totalDocuments);
        
        return ResponseEntity.ok(dashboardStats);
    }
} 