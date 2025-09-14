package com.barebonebatch.common.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Work {
    private String id;
    private String systemCode;
    private String context;
    private String workClassName;
    private String description;
    private String isActive;
    private Integer iden;
}
