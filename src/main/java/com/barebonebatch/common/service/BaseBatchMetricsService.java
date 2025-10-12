package com.barebonebatch.common.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class BaseBatchMetricsService {

    private final MeterRegistry meterRegistry;

    // Counters
    private final Counter totalJobsCounter;
    private final Counter totalStepsCounter;
    private final Counter itemsProcessedCounter;
    private final Counter itemsReadCounter;
    private final Counter itemsWrittenCounter;
    private final Counter skipCounter;
    private final Counter errorCounter;

    // Timers
    private final Timer jobExecutionTimer;
    private final Timer stepExecutionTimer;
    private final Timer chunkProcessingTimer;

    // Gauges
    private final AtomicLong activeJobs = new AtomicLong(0);
    private final AtomicLong activeSteps = new AtomicLong(0);

    @Autowired
    public BaseBatchMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize counters
        this.totalJobsCounter = Counter.builder("batch.jobs.total")
                .description("Total number of jobs executed")
                .register(meterRegistry);

        this.totalStepsCounter = Counter.builder("batch.steps.total")
                .description("Total number of steps executed")
                .register(meterRegistry);

        this.itemsProcessedCounter = Counter.builder("batch.items.processed")
                .description("Total number of items processed")
                .register(meterRegistry);

        this.itemsReadCounter = Counter.builder("batch.items.read")
                .description("Total number of items read")
                .register(meterRegistry);

        this.itemsWrittenCounter = Counter.builder("batch.items.written")
                .description("Total number of items written")
                .register(meterRegistry);

        this.skipCounter = Counter.builder("batch.items.skipped")
                .description("Total number of items skipped")
                .register(meterRegistry);

        this.errorCounter = Counter.builder("batch.errors.total")
                .description("Total number of errors")
                .register(meterRegistry);

        // Initialize timers
        this.jobExecutionTimer = Timer.builder("batch.job.execution.time")
                .description("Time taken for job execution")
                .register(meterRegistry);

        this.stepExecutionTimer = Timer.builder("batch.step.execution.time")
                .description("Time taken for step execution")
                .register(meterRegistry);

        this.chunkProcessingTimer = Timer.builder("batch.chunk.processing.time")
                .description("Time taken for chunk processing")
                .register(meterRegistry);

        // Initialize gauges
        Gauge.builder("batch.jobs.active", activeJobs, AtomicLong::get)
                .description("Number of currently active jobs")
                .register(meterRegistry);

        Gauge.builder("batch.steps.active", activeSteps, AtomicLong::get)
                .description("Number of currently active steps")
                .register(meterRegistry);
    }

    // Job metrics
    public void incrementJobCount(String jobName) {
        totalJobsCounter.increment();

        Counter.builder("batch.jobs.by.name")
                .tag("jobName", jobName)
                .register(meterRegistry)
                .increment();
    }

    public Timer.Sample startJobExecutionTimer() {
        activeJobs.incrementAndGet();
        return Timer.start(meterRegistry);
    }

    public void stopJobExecutionTimer(Timer.Sample sample, String jobName, String status) {
        activeJobs.decrementAndGet();
        sample.stop(Timer.builder("batch.job.execution.duration")
                .tag("jobName", jobName)
                .tag("status", status)
                .register(meterRegistry));
    }

    // Step metrics
    public void incrementStepCount(String stepName) {
        totalStepsCounter.increment();
        activeSteps.incrementAndGet();

        Counter.builder("batch.steps.by.name")
                .tag("stepName", stepName)
                .register(meterRegistry)
                .increment();
    }

    public Timer.Sample startStepExecutionTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopStepExecutionTimer(Timer.Sample sample, String stepName, String status) {
        activeSteps.decrementAndGet();
        sample.stop(Timer.builder("batch.step.execution.duration")
                .tag("stepName", stepName)
                .tag("status", status)
                .register(meterRegistry));
    }

    // Item processing metrics
    public void incrementItemsProcessed(long count, String stepName) {
        itemsProcessedCounter.increment(count);

        Counter.builder("batch.items.processed.by.step")
                .tag("stepName", stepName)
                .register(meterRegistry)
                .increment(count);
    }

    public void incrementItemsRead(long count, String stepName) {
        itemsReadCounter.increment(count);
    }

    public void incrementItemsWritten(long count, String stepName) {
        itemsWrittenCounter.increment(count);
    }

    // Error and skip metrics
    public void incrementSkipCount(String stepName, String skipType) {
        skipCounter.increment();

        Counter.builder("batch.items.skipped.by.type")
                .tag("stepName", stepName)
                .tag("skipType", skipType)
                .register(meterRegistry)
                .increment();
    }

    public void incrementErrorCount(String stepName, String errorType) {
        errorCounter.increment();

        Counter.builder("batch.errors.by.type")
                .tag("stepName", stepName)
                .tag("errorType", errorType)
                .register(meterRegistry)
                .increment();
    }

    // Chunk processing metrics
    public Timer.Sample startChunkProcessingTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopChunkProcessingTimer(Timer.Sample sample, String stepName, int chunkSize) {
        sample.stop(Timer.builder("batch.chunk.processing.duration")
                .tag("stepName", stepName)
                .tag("chunkSize", String.valueOf(chunkSize))
                .register(meterRegistry));
    }

    // Memory metrics
    public void recordMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();

        meterRegistry.gauge("batch.memory.used.bytes", usedMemory);
        meterRegistry.gauge("batch.memory.max.bytes", maxMemory);
        meterRegistry.gauge("batch.memory.usage.percent",
                (double) usedMemory / maxMemory * 100);
    }
}