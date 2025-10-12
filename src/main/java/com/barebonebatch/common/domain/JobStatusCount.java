package com.barebonebatch.common.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobStatusCount {
    private String jobName;
    private String status;
    private int count;
}
