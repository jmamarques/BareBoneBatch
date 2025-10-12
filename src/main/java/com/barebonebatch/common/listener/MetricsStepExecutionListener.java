package com.barebonebatch.common.listener;

import com.barebonebatch.common.service.BaseBatchMetricsService;
import io.micrometer.core.instrument.Timer;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class MetricsStepExecutionListener implements StepExecutionListener {

    private final BaseBatchMetricsService metricsService;

    private final ThreadLocal<Timer.Sample> stepTimer = new ThreadLocal<>();
    private final ThreadLocal<String> stepName = new ThreadLocal<>();

    public MetricsStepExecutionListener(BaseBatchMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        String name = stepExecution.getStepName();
        stepName.set(name);
        stepTimer.set(metricsService.startStepExecutionTimer());
        metricsService.incrementStepCount(name);

        System.out.println("Starting step: " + name);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        String name = stepName.get();
        String status = stepExecution.getStatus().toString();

        metricsService.stopStepExecutionTimer(stepTimer.get(), name, status);

        // Record step metrics
        metricsService.incrementItemsRead(stepExecution.getReadCount(), name);
        metricsService.incrementItemsWritten(stepExecution.getWriteCount(), name);
        metricsService.incrementItemsProcessed(stepExecution.getWriteCount(), name);

        // Record skips
        if (stepExecution.getReadSkipCount() > 0) {
            metricsService.incrementSkipCount(name, "READ");
        }
        if (stepExecution.getProcessSkipCount() > 0) {
            metricsService.incrementSkipCount(name, "PROCESS");
        }
        if (stepExecution.getWriteSkipCount() > 0) {
            metricsService.incrementSkipCount(name, "WRITE");
        }

        log.info("Completed step: {}, Read: {}, Written: {}, Skips: {}",
                name, stepExecution.getReadCount(), stepExecution.getWriteCount(),
                stepExecution.getSkipCount());

        // Cleanup
        stepTimer.remove();
        stepName.remove();

        return stepExecution.getExitStatus();
    }
}