package com.barebonebatch.common.controller;

import com.barebonebatch.common.dao.BatchMetricsDao;
import com.barebonebatch.common.domain.JobStatusCount;
import io.micrometer.core.instrument.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final MeterRegistry meterRegistry;

    private final BatchMetricsDao batchMetricsMapper;

    public MetricsController(MeterRegistry meterRegistry, BatchMetricsDao batchMetricsMapper) {
        this.meterRegistry = meterRegistry;
        this.batchMetricsMapper = batchMetricsMapper;
    }

    @GetMapping("/batch/summary")
    public Map<String, Object> getBatchSummary() {
        Map<String, Object> summary = new HashMap<>();

        try {
            List<JobStatusCount> jobStatusCounts = batchMetricsMapper.getJobStatusCounts();

            summary.put("jobStatusCounts", jobStatusCounts);
            summary.put("recentExecutions", batchMetricsMapper.getRecentJobExecutions());
            summary.put("stepDurations", batchMetricsMapper.getStepDurations());

        } catch (Exception e) {
            summary.put("error", "Failed to retrieve batch summary: " + e.getMessage());
        }

        return summary;
    }

    @GetMapping("/custom")
    public Map<String, Object> getCustomMetrics() {
        Map<String, Object> customMetrics = new HashMap<>();

        meterRegistry.getMeters().forEach(meter -> {
            String meterName = meter.getId().getName();
            if (meterName.startsWith("batch.")) {
                customMetrics.put(meterName, getMeterValue(meter));
            }
        });

        Runtime runtime = Runtime.getRuntime();
        customMetrics.put("memory.used", runtime.totalMemory() - runtime.freeMemory());
        customMetrics.put("memory.max", runtime.maxMemory());
        customMetrics.put("memory.free", runtime.freeMemory());

        return customMetrics;
    }

    @GetMapping("/health/detailed")
    public Map<String, Object> getDetailedHealth() {
        Map<String, Object> healthInfo = new HashMap<>();

        healthInfo.put("timestamp", new Date());
        healthInfo.put("application", "Batch Monitoring Service");

        Runtime runtime = Runtime.getRuntime();
        healthInfo.put("memoryUsedMB", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024);
        healthInfo.put("memoryMaxMB", runtime.maxMemory() / 1024 / 1024);
        healthInfo.put("memoryFreeMB", runtime.freeMemory() / 1024 / 1024);

        healthInfo.put("threadCount", Thread.activeCount());

        return healthInfo;
    }

    private Object getMeterValue(Meter meter) {
        if (meter instanceof Counter) {
            return ((Counter) meter).count();
        } else if (meter instanceof Timer) {
            return ((Timer) meter).totalTime(TimeUnit.MILLISECONDS);
        } else if (meter instanceof Gauge) {
            return ((Gauge) meter).value();
        }
        return null;
    }
}
