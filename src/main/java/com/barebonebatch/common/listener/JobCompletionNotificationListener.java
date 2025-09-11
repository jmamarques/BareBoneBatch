package com.barebonebatch.common.listener;

import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class JobCompletionNotificationListener implements JobExecutionListener {

    @Autowired
    private UserMapper userMapper;

    /**
     * Called before a job is executed.
     * It logs the job's start time and status to the `import_job` table.
     *
     * @param jobExecution The context of the job execution.
     */
    @Override
    public void beforeJob(JobExecution jobExecution) {
        System.out.println("Job started: " + jobExecution.getJobInstance().getJobName());
        Map<String, Object> jobRun = new HashMap<>();
        jobRun.put("id", jobExecution.getJobId());
        jobRun.put("jobName", jobExecution.getJobInstance().getJobName());
        jobRun.put("status", jobExecution.getStatus().name());
        jobRun.put("startTime", new Timestamp(jobExecution.getStartTime().getTime()));
        jobRun.put("endTime", null); // Set end time to null initially.
        jobRun.put("exitMessage", "Job started successfully.");
        userMapper.insertJobRun(jobRun);
    }

    /**
     * Called after a job is completed, regardless of its status (success or failure).
     * It updates the `import_job` table with the final status and end time.
     *
     * @param jobExecution The context of the job execution.
     */
    @Override
    public void afterJob(JobExecution jobExecution) {
        System.out.println("Job finished with status: " + jobExecution.getStatus());
        Map<String, Object> jobRun = new HashMap<>();
        jobRun.put("id", jobExecution.getJobId());
        jobRun.put("status", jobExecution.getStatus().name());
        jobRun.put("endTime", new Timestamp(jobExecution.getEndTime().getTime()));

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
        userMapper.updateJobRun(jobRun);
    }
}