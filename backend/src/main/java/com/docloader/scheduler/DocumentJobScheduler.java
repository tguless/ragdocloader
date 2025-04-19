package com.docloader.scheduler;

import com.docloader.model.DocumentJob;
import com.docloader.service.DocumentJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentJobScheduler {

    private final DocumentJobService documentJobService;
    private final DocumentProcessingService documentProcessingService;

    /**
     * Checks for scheduled jobs that are due and starts processing them
     * Runs every minute
     */
    @Scheduled(fixedRate = 60000)
    public void processScheduledJobs() {
        log.debug("Checking for scheduled jobs...");
        
        List<DocumentJob> dueJobs = documentJobService.getScheduledJobsDue();
        
        if (!dueJobs.isEmpty()) {
            log.info("Found {} scheduled jobs to process", dueJobs.size());
            
            for (DocumentJob job : dueJobs) {
                log.info("Starting scheduled job: {}", job.getId());
                // Update the job status to PROCESSING
                documentJobService.updateJobStatus(job.getId(), DocumentJob.JobStatus.PROCESSING);
                
                // Start processing the job asynchronously
                documentProcessingService.processJobAsync(job.getId());
            }
        }
    }
    
    /**
     * Checks for any pending jobs and starts processing them
     * Runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000)
    public void processPendingJobs() {
        log.debug("Checking for pending jobs...");
        
        List<DocumentJob> pendingJobs = documentJobService.getJobsByStatus(DocumentJob.JobStatus.PENDING);
        
        if (!pendingJobs.isEmpty()) {
            log.info("Found {} pending jobs to process", pendingJobs.size());
            
            for (DocumentJob job : pendingJobs) {
                log.info("Starting pending job: {}", job.getId());
                // Update the job status to PROCESSING
                documentJobService.updateJobStatus(job.getId(), DocumentJob.JobStatus.PROCESSING);
                
                // Start processing the job asynchronously
                documentProcessingService.processJobAsync(job.getId());
            }
        }
    }
} 