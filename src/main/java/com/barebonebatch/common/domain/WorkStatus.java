package com.barebonebatch.common.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class WorkStatus {

    private Integer id;

    private String workIden;

    private String fileIden;

    private Integer statusCode;

    private LocalDateTime creationDate;

    private LocalDateTime beginDate;

    private LocalDateTime endDate;

    private String errorText;

}
