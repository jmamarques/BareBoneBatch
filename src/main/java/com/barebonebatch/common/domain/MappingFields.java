package com.barebonebatch.common.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MappingFields {
    private Integer mappingFk;
    private Integer iden;
    private String id;
    private String description;
    private String property;
    private String type;
    private String transformer;
    private String pattern;
    private String mandatory;
    private String enable;
    private Integer offset;
    private Integer length;
}
