package com.barebonebatch.common.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Mapping {

    private Integer iden;
    private String id;
    private String mappingType;
    private List<MappingFields> fields;
}
