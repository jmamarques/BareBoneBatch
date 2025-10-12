package com.barebonebatch.common.config;

import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Log4j2
public class MetricsLogger implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        List<String> metrics = Arrays.asList(
                "application.ready.time",
                "application.started.time",
                "batch.chunk.processing.time",
                "batch.errors.total",
                "batch.items.processed",
                "batch.items.read",
                "batch.items.skipped",
                "batch.items.written",
                "batch.job.execution.time",
                "batch.jobs.active",
                "batch.jobs.total",
                "batch.step.execution.time",
                "batch.steps.active",
                "batch.steps.total",
                "disk.free",
                "disk.total",
                "executor.active",
                "executor.completed",
                "executor.pool.core",
                "executor.pool.max",
                "executor.pool.size",
                "executor.queue.remaining",
                "executor.queued",
                "hikaricp.connections",
                "hikaricp.connections.acquire",
                "hikaricp.connections.active",
                "hikaricp.connections.creation",
                "hikaricp.connections.idle",
                "hikaricp.connections.max",
                "hikaricp.connections.min",
                "hikaricp.connections.pending",
                "hikaricp.connections.timeout",
                "hikaricp.connections.usage",
                "http.server.requests",
                "http.server.requests.active",
                "jdbc.connections.active",
                "jdbc.connections.idle",
                "jdbc.connections.max",
                "jdbc.connections.min",
                "jvm.buffer.count",
                "jvm.buffer.memory.used",
                "jvm.buffer.total.capacity",
                "jvm.classes.loaded",
                "jvm.classes.unloaded",
                "jvm.compilation.time",
                "jvm.gc.live.data.size",
                "jvm.gc.max.data.size",
                "jvm.gc.memory.allocated",
                "jvm.gc.memory.promoted",
                "jvm.gc.overhead",
                "jvm.info",
                "jvm.memory.committed",
                "jvm.memory.max",
                "jvm.memory.usage.after.gc",
                "jvm.memory.used",
                "jvm.threads.daemon",
                "jvm.threads.live",
                "jvm.threads.peak",
                "jvm.threads.started",
                "jvm.threads.states",
                "logback.events",
                "process.cpu.time",
                "process.cpu.usage",
                "process.start.time",
                "process.uptime",
                "spring.batch.job.launch.count",
                "system.cpu.count",
                "system.cpu.usage",
                "tasks.scheduled.execution",
                "tasks.scheduled.execution.active",
                "tomcat.sessions.active.current",
                "tomcat.sessions.active.max",
                "tomcat.sessions.alive.max",
                "tomcat.sessions.created",
                "tomcat.sessions.expired",
                "tomcat.sessions.rejected"
        );

        log.info("=================================================================");
        log.info("Actuator Metrics Links:");
        log.info("=================================================================");
        metrics.forEach(metric -> log.info("http://localhost:8080/actuator/metrics/{}", metric));
        log.info("=================================================================");
    }
}
