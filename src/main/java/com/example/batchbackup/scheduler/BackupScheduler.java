package com.example.batchbackup.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BackupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(BackupScheduler.class);

    private final JobLauncher jobLauncher;
    private final Job orderBackupJob;
    private final Job customerBackupJob;
    private final Job productBackupJob;

    public BackupScheduler(
            JobLauncher jobLauncher,
            @Qualifier("orderBackupJob") Job orderBackupJob,
            @Qualifier("customerBackupJob") Job customerBackupJob,
            @Qualifier("productBackupJob") Job productBackupJob) {
        this.jobLauncher = jobLauncher;
        this.orderBackupJob = orderBackupJob;
        this.customerBackupJob = customerBackupJob;
        this.productBackupJob = productBackupJob;
    }

    // Run daily at 2 AM
    @Scheduled(cron = "${backup.schedule.cron:0 0 2 * * ?}")
    public void runBackupJobs() {
        logger.info("Starting scheduled backup jobs...");

        try {
            // Run Order Backup Job
            JobParameters orderParams = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .addString("jobName", "orderBackup")
                    .toJobParameters();
            jobLauncher.run(orderBackupJob, orderParams);
            logger.info("Order backup job completed successfully");

            // Run Customer Backup Job
            JobParameters customerParams = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .addString("jobName", "customerBackup")
                    .toJobParameters();
            jobLauncher.run(customerBackupJob, customerParams);
            logger.info("Customer backup job completed successfully");

            // Run Product Backup Job
            JobParameters productParams = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .addString("jobName", "productBackup")
                    .toJobParameters();
            jobLauncher.run(productBackupJob, productParams);
            logger.info("Product backup job completed successfully");

        } catch (Exception e) {
            logger.error("Error running backup jobs: {}", e.getMessage(), e);
        }
    }

    // Manual trigger endpoint for testing
    public void runOrderBackup() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .addString("jobName", "orderBackup")
                .toJobParameters();
        jobLauncher.run(orderBackupJob, params);
    }

    public void runCustomerBackup() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .addString("jobName", "customerBackup")
                .toJobParameters();
        jobLauncher.run(customerBackupJob, params);
    }

    public void runProductBackup() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .addString("jobName", "productBackup")
                .toJobParameters();
        jobLauncher.run(productBackupJob, params);
    }
}
