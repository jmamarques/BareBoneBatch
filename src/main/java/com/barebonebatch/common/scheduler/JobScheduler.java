package com.mycompany.mybatchapp.common.scheduler;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
@EnableScheduling
public class JobScheduler {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job fileProcessingJob;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ApplicationContext applicationContext;

    @Scheduled(fixedRate = 10000) // Run every 10 seconds
    @Transactional
    public void checkForNewJobs() {
        // Query the database for new job requests
        String sql = "SELECT job_id, input_file_path FROM JOB_REQUESTS WHERE status = 'PENDING'";
        List<Map<String, Object>> newJobs = jdbcTemplate.queryForList(sql);

        for (Map<String, Object> jobRequest : newJobs) {
            String jobId = (String) jobRequest.get("job_id");
            String inputFilePath = (String) jobRequest.get("input_file_path");
            String jobBeanName = (String) jobRequest.get("job_bean_name");

            try {
                // Get the Job bean from the application context
                Job jobToRun = applicationContext.getBean(jobBeanName, Job.class);

                // Build unique job parameters
                JobParameters jobParameters = new JobParametersBuilder()
                        .addString("jobId", jobId)
                        .addString("inputFile", inputFilePath)
                        .addLong("runTime", System.currentTimeMillis())
                        .toJobParameters();

                // 2. Update status BEFORE launching
                jdbcTemplate.update("UPDATE JOB_REQUESTS SET status = 'STARTED' WHERE job_id = ?", jobId);

                // Launch the job (can be done asynchronously)
                jobLauncher.run(jobToRun, jobParameters);

            } catch (JobExecutionException e) {
                System.err.println("Failed to launch job with ID " + jobId + ": " + e.getMessage());
                // Update status to FAILED in case of launch error
                jdbcTemplate.update("UPDATE JOB_REQUESTS SET status = 'FAILED' WHERE job_id = ?", jobId);
            } catch (Exception e) {
                System.err.println("Job bean '" + jobBeanName + "' not found for job " + jobId);
                // Update status to ERROR for non-launch errors
                jdbcTemplate.update("UPDATE JOB_REQUESTS SET status = 'ERROR' WHERE job_id = ?", jobId);
            }
        }
    }
}