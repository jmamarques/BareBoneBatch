package com.barebonebatch.common.listener;

import com.barebonebatch.common.service.BaseBatchMetricsService;
import io.micrometer.core.instrument.Timer;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class MetricsJobExecutionListener implements JobExecutionListener {

    private final BaseBatchMetricsService metricsService;

    private final ThreadLocal<Timer.Sample> jobTimer = new ThreadLocal<>();
    private final ThreadLocal<String> jobName = new ThreadLocal<>();

    public MetricsJobExecutionListener(BaseBatchMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String name = jobExecution.getJobInstance().getJobName();
        jobName.set(name);
        jobTimer.set(metricsService.startJobExecutionTimer());
        metricsService.incrementJobCount(name);

        log.info("Starting job: {}", name);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String name = jobName.get();
        String status = jobExecution.getStatus().toString();

        metricsService.stopJobExecutionTimer(jobTimer.get(), name, status);

        // Record job-level metrics
        metricsService.recordMemoryUsage();

        log.info("Completed job: {} with status: {}", name, status);

        // Cleanup
        jobTimer.remove();
        jobName.remove();
    }
}