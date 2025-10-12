package com.barebonebatch.common.domain;

import lombok.Data;

import java.util.Date;

@Data
public class JobExecutionHistory {
    private String jobName;
    private Date startTime;
    private Date endTime;
    private String status;
    private String exitCode;
}
