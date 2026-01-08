package com.example.batchbackup.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/backup")
public class BackupController {

    private static final Logger logger = LoggerFactory.getLogger(BackupController.class);

    private final JobLauncher jobLauncher;
    private final Job orderBackupJob;
    private final Job customerBackupJob;
    private final Job productBackupJob;

    public BackupController(
            JobLauncher jobLauncher,
            @Qualifier("orderBackupJob") Job orderBackupJob,
            @Qualifier("customerBackupJob") Job customerBackupJob,
            @Qualifier("productBackupJob") Job productBackupJob) {
        this.jobLauncher = jobLauncher;
        this.orderBackupJob = orderBackupJob;
        this.customerBackupJob = customerBackupJob;
        this.productBackupJob = productBackupJob;
    }

    @PostMapping("/run")
    public ResponseEntity<Map<String, Object>> runBackupJobs() {
        logger.info("Manual backup jobs triggered via API");
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Run Order Backup Job
            JobParameters orderParams = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .addString("jobName", "orderBackup")
                    .toJobParameters();
            var orderExecution = jobLauncher.run(orderBackupJob, orderParams);
            logger.info("Order backup job completed: {}", orderExecution.getStatus());
            response.put("orderBackup", orderExecution.getStatus().toString());

            // Run Customer Backup Job
            JobParameters customerParams = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .addString("jobName", "customerBackup")
                    .toJobParameters();
            var customerExecution = jobLauncher.run(customerBackupJob, customerParams);
            logger.info("Customer backup job completed: {}", customerExecution.getStatus());
            response.put("customerBackup", customerExecution.getStatus().toString());

            // Run Product Backup Job
            JobParameters productParams = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .addString("jobName", "productBackup")
                    .toJobParameters();
            var productExecution = jobLauncher.run(productBackupJob, productParams);
            logger.info("Product backup job completed: {}", productExecution.getStatus());
            response.put("productBackup", productExecution.getStatus().toString());

            response.put("status", "SUCCESS");
            response.put("message", "All backup jobs completed successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error running backup jobs", e);
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
