package com.barebonebatch.common.listener;

import com.barebonebatch.common.dao.JobDao;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@Log4j2
public class JobCompletionNotificationListener implements JobExecutionListener {

    /**
     * Called before a job is executed.
     * It logs the job's start time and status to the `import_job` table.
     *
     * @param jobExecution The context of the job execution.
     */
    @Override
    public void beforeJob(JobExecution jobExecution) {
       log.info("Job started: " + jobExecution.getJobInstance().getJobName());
        Map<String, Object> jobRun = new HashMap<>();
        jobRun.put("id", jobExecution.getJobId());
        jobRun.put("jobName", jobExecution.getJobInstance().getJobName());
        jobRun.put("status", jobExecution.getStatus().name());
        jobRun.put("startTime", LocalDateTime.now());
        jobRun.put("endTime", null); // Set end time to null initially.
        jobRun.put("exitMessage", "Job started successfully.");
//        jobDao.insertJobRun(jobRun);
    }

    /**
     * Called after a job is completed, regardless of its status (success or failure).
     * It updates the `import_job` table with the final status and end time.
     *
     * @param jobExecution The context of the job execution.
     */
    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("Job finished with status: " + jobExecution.getStatus());
        Map<String, Object> jobRun = new HashMap<>();
        jobRun.put("id", jobExecution.getJobId());
        jobRun.put("status", jobExecution.getStatus().name());
        jobRun.put("endTime", LocalDateTime.now());

        String exitMessage = "Job finished successfully.";
        if (!jobExecution.getExitStatus().getExitCode().equals("COMPLETED")) {
            // Handle exceptions and failures.
            if (!jobExecution.getFailureExceptions().isEmpty()) {
                exitMessage = "Job failed with exception: " + jobExecution.getFailureExceptions().get(0).getMessage();
            } else {
                exitMessage = "Job finished with status: " + jobExecution.getExitStatus().getExitCode();
            }
        }
        jobRun.put("exitMessage", exitMessage);
//        JobDao.updateJobRun(jobRun);
    }
}