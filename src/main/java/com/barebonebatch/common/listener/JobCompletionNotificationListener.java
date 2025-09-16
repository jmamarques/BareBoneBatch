package com.barebonebatch.common.listener;

import com.barebonebatch.common.dao.JobDao;
import com.barebonebatch.common.domain.WorkStatus;
import com.barebonebatch.common.util.Constants;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * A listener that handles actions before and after a job execution.
 * It is responsible for updating the overall status of the {@link WorkStatus} entity in the database.
 * This provides the final, summary outcome of the entire job.
 */
@Component
@Log4j2
public class JobCompletionNotificationListener implements JobExecutionListener {

    private final JobDao jobDao;

    public JobCompletionNotificationListener(JobDao jobDao) {
        this.jobDao = jobDao;
    }

    /**
     * Executes before the job starts.
     * This implementation logs the job's start and updates the corresponding {@link WorkStatus}
     * record to the 'PROCESSING' state.
     *
     * @param jobExecution The context of the current job execution.
     */
    @Override
    public void beforeJob(JobExecution jobExecution) {
        Long wstIden = jobExecution.getJobParameters().getLong(Constants.WST_IDEN);
        log.info("Job started: {} with id: {}", jobExecution.getJobInstance().getJobName(), wstIden);
        Optional<WorkStatus> optionalWorkStatus = jobDao.getWorkStatusByWstIden(wstIden);
        if (optionalWorkStatus.isPresent()) {
            WorkStatus workStatus = optionalWorkStatus.get();
            workStatus.setWstBegiDate(jobExecution.getStartTime());
            workStatus.setWstStatCode(Constants.PROCESSING);
            jobDao.updateWorkStatus(workStatus);
        }
    }

    /**
     * Executes after the job completes (whether it succeeds, fails, or completes with skips).
     * <p>
     * This method is responsible for setting the final status on the {@link WorkStatus} record.
     * It determines the outcome based on the job's exit status and whether any items were skipped.
     * <ul>
     *     <li><b>COMPLETED:</b> The job finished successfully. If skips occurred in any step, the status is set to 'SUCCESS_WITH_ERRORS'.</li>
     *     <li><b>FAILED:</b> The job failed. The status is set to 'ERROR', and the exception message is recorded.</li>
     * </ul>
     * Note: The detailed error messages for each skipped line are persisted by the 'logSkippedItemsStep', not by this listener.
     *
     * @param jobExecution The context of the completed job execution.
     */
    @Override
    public void afterJob(JobExecution jobExecution) {
        Long wstIden = jobExecution.getJobParameters().getLong(Constants.WST_IDEN);
        log.info("Job finished with status: {} with id: {} and status: {}", jobExecution.getJobInstance().getJobName(), wstIden, jobExecution.getStatus().name());
        Optional<WorkStatus> optionalWorkStatus = jobDao.getWorkStatusByWstIden(wstIden);
        if (optionalWorkStatus.isPresent()) {
            WorkStatus workStatus = optionalWorkStatus.get();
            workStatus.setWstEndxDate(LocalDateTime.now());

            String exitMessage = "";
            Integer status = Constants.SUCCESS;
            if (!jobExecution.getExitStatus().getExitCode().equals("COMPLETED")) {
                status = Constants.ERROR;
                if (!jobExecution.getFailureExceptions().isEmpty()) {
                    exitMessage = "Job failed with exception: " + jobExecution.getFailureExceptions().get(0).getMessage();
                } else {
                    exitMessage = "Job finished with status: " + jobExecution.getExitStatus().getExitCode();
                }
            } else {
                // Check if any step in the job had skipped items.
                if (jobExecution.getStepExecutions().stream().anyMatch(step -> step.getSkipCount() > 0)) {
                    status = Constants.SUCCESS_WITH_ERRORS;
                    exitMessage = "Job completed with one or more skipped items.";
                }
            }
            workStatus.setWstStatCode(status);
            workStatus.setWstErrorText(exitMessage);
            jobDao.updateWorkStatus(workStatus);
        }
    }
}
