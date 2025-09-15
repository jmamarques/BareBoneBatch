package com.barebonebatch.common.scheduler;

import com.barebonebatch.common.dao.JobDao;
import com.barebonebatch.common.domain.Work;
import com.barebonebatch.common.domain.WorkStatus;
import com.barebonebatch.common.exception.InvalidWorkIdentifierException;
import com.barebonebatch.common.exception.JobNotFoundException;
import com.barebonebatch.common.exception.WorkNotFoundException;
import com.barebonebatch.common.util.Constants;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
@Log4j2
public class JobScheduler {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobDao jobDao;

    @Autowired
    private ApplicationContext applicationContext;

    @Scheduled(fixedRate = 10000)
    @Transactional
    public void checkForNewJobs() {
        log.info("Checking for New Jobs");
        WorkStatus jobRequest = jobDao.getNextWorkStatusPending();
        if (jobRequest == null) {
            return; // No pending jobs
        }

        try {
            processJobRequest(jobRequest);
        } catch (Exception e) {
            log.error("Error processing job request with ID {}: {}", jobRequest.getWstIden(), e.getMessage());
            updateWorkStatusWithError(jobRequest, e.getMessage());
        }
    }

    private void processJobRequest(WorkStatus jobRequest) {
        String[] workIdentifierParts = getWorkIdentifierParts(jobRequest.getWstFileIden());

        updateWorkStatusToProcessing(jobRequest);

        List<Work> works = findWorks(workIdentifierParts[0]);

        for (Work work : works) {
            launchJobForWork(work, jobRequest.getWstIden());
        }
        jobDao.updateWorkStatusEnd(jobRequest);
    }

    private String[] getWorkIdentifierParts(String workIdentifier) {
        String[] parts = StringUtils.split(workIdentifier, Constants.DOT);
        if (parts == null || parts.length == 0) {
            throw new InvalidWorkIdentifierException("Work identifier '" + workIdentifier + "' is invalid.");
        }
        return parts;
    }

    private List<Work> findWorks(String fileIdentifier) {
        List<Work> works = jobDao.getWorkByFileIden(fileIdentifier);
        if (works == null || works.isEmpty()) {
            throw new WorkNotFoundException("No work found for file identifier '" + fileIdentifier + "'.");
        }
        return works;
    }

    private void updateWorkStatusToProcessing(WorkStatus jobRequest) {
        jobRequest.setWstBegiDate(LocalDateTime.now());
        jobRequest.setWstStatCode(Constants.PROCESSING);
        jobDao.updateWorkStatus(jobRequest);
    }

    private void launchJobForWork(Work work, Integer workStatusId) {
        try {
            Job jobToRun = findJobBean(work.getWorkClassName());
            JobParameters jobParameters = buildJobParameters(workStatusId);

            log.debug("Launching job with ID {}", workStatusId);
            jobLauncher.run(jobToRun, jobParameters);
        } catch (JobExecutionException e) {
            throw new RuntimeException("Failed to launch job with ID " + workStatusId, e);
        } catch (BeansException e) {
            throw new JobNotFoundException("Job bean '" + work.getWorkClassName() + "' not found for job " + workStatusId, e);
        }
    }

    private Job findJobBean(String jobClassName) {
        try {
            return applicationContext.getBean(jobClassName, Job.class);
        } catch (BeansException e) {
            throw new JobNotFoundException("Job setup for work class '" + jobClassName + "' does not exist in the project.", e);
        }
    }

    private JobParameters buildJobParameters(Integer workStatusId) {
        return new JobParametersBuilder()
                .addLong(Constants.WST_IDEN, Long.valueOf(workStatusId))
                .addLong(Constants.START_DATE, System.currentTimeMillis())
                .toJobParameters();
    }

    private void updateWorkStatusWithError(WorkStatus jobRequest, String error) {
        jobRequest.setWstErrorText(error);
        jobRequest.setWstStatCode(Constants.ERROR);
        jobDao.updateWorkStatus(jobRequest);
    }
}
