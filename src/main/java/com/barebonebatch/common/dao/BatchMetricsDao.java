package com.barebonebatch.common.dao;

import com.barebonebatch.common.domain.JobExecutionHistory;
import com.barebonebatch.common.domain.JobStatusCount;
import com.barebonebatch.common.domain.StepDuration;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BatchMetricsDao {

    @Select("SELECT COUNT(*) FROM BATCH_JOB_INSTANCE WHERE JOB_NAME = #{jobName}")
    int countJobInstancesByName(@Param("jobName") String jobName);

    @Select("SELECT COUNT(*) FROM BATCH_JOB_EXECUTION WHERE STATUS = #{status}")
    int countJobExecutionsByStatus(@Param("status") String status);

    @Select("SELECT JOB_NAME, STATUS, COUNT(*) as count " +
            "FROM BATCH_JOB_EXECUTION je " +
            "JOIN BATCH_JOB_INSTANCE ji ON je.JOB_INSTANCE_ID = ji.JOB_INSTANCE_ID " +
            "GROUP BY JOB_NAME, STATUS")
    @Results({
            @Result(property = "jobName", column = "JOB_NAME"),
            @Result(property = "status", column = "STATUS"),
            @Result(property = "count", column = "count")
    })
    List<JobStatusCount> getJobStatusCounts();

    @Select("SELECT STEP_NAME, STATUS, AVG((END_TIME - START_TIME) * 86400) as avg_duration_seconds " +
            "FROM BATCH_STEP_EXECUTION " +
            "WHERE END_TIME IS NOT NULL AND START_TIME IS NOT NULL " +
            "GROUP BY STEP_NAME, STATUS")
    @Results({
            @Result(property = "stepName", column = "STEP_NAME"),
            @Result(property = "status", column = "STATUS"),
            @Result(property = "avgDurationSeconds", column = "avg_duration_seconds")
    })
    List<StepDuration> getStepDurations();

    @Select("SELECT JOB_NAME, START_TIME, END_TIME, STATUS, EXIT_CODE " +
            "FROM BATCH_JOB_EXECUTION je " +
            "JOIN BATCH_JOB_INSTANCE ji ON je.JOB_INSTANCE_ID = ji.JOB_INSTANCE_ID " +
            "ORDER BY START_TIME DESC " +
            "FETCH FIRST 50 ROWS ONLY")
    @Results({
            @Result(property = "jobName", column = "JOB_NAME"),
            @Result(property = "startTime", column = "START_TIME"),
            @Result(property = "endTime", column = "END_TIME"),
            @Result(property = "status", column = "STATUS"),
            @Result(property = "exitCode", column = "EXIT_CODE")
    })
    List<JobExecutionHistory> getRecentJobExecutions();
}
