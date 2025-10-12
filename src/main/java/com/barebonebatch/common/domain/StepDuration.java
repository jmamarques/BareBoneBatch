package com.barebonebatch.common.domain;

import lombok.Data;

@Data
public class StepDuration {
    private String stepName;
    private String status;
    private double avgDurationSeconds;
}
