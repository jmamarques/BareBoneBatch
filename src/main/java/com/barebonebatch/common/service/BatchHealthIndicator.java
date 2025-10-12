package com.barebonebatch.common.service;

import com.barebonebatch.common.dao.BatchMetricsDao;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class BatchHealthIndicator implements HealthIndicator {

    private final BatchMetricsDao batchMetricsDao;

    private final DataSource dataSource;

    private static final int MAX_FAILED_JOBS = 10;

    public BatchHealthIndicator(BatchMetricsDao batchMetricsDao, DataSource dataSource) {
        this.batchMetricsDao = batchMetricsDao;
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try {
            List<String> issues = new ArrayList<>();
            checkFailedJobs(issues);
            checkDatabase(issues);
            checkBatchTables(issues);

            if (issues.isEmpty()) {
                return Health.up()
                        .withDetail("message", "Batch system is healthy")
                        .withDetail("database", "Connected to Oracle")
                        .withDetail("batchTables", "All tables accessible")
                        .build();
            } else {
                return Health.down()
                        .withDetail("message", "Batch system has issues")
                        .withDetail("issues", issues)
                        .build();
            }

        } catch (Exception e) {
            return Health.down(e).build();
        }
    }

    private void checkFailedJobs(List<String> issues) {
        try {
            int failedJobsCount = batchMetricsDao.countJobExecutionsByStatus("FAILED");
            if (failedJobsCount > MAX_FAILED_JOBS) {
                issues.add(String.format("Too many failed jobs: %d", failedJobsCount));
            }
        } catch (Exception e) {
            issues.add("Cannot query failed jobs: " + e.getMessage());
        }
    }

    private void checkDatabase(List<String> issues) {
        try {
            dataSource.getConnection().close();
        } catch (SQLException e) {
            issues.add("Database connectivity issue: " + e.getMessage());
        }
    }

    private void checkBatchTables(List<String> issues) {
        try {
            batchMetricsDao.countJobInstancesByName("test");
        } catch (Exception e) {
            issues.add("Batch tables issue: " + e.getMessage());
        }
    }
}
