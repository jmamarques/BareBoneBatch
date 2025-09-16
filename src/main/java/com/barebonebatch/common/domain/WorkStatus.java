package com.barebonebatch.common.domain;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class WorkStatus {

    private Integer wstIden;
    private String wstWorkIden;
    private String wstFileIden;
    private Integer wstStatCode;
    private LocalDateTime wstCreaDate;
    private LocalDateTime wstBegiDate;
    private LocalDateTime wstEndxDate;
    private String wstErrorText;
    private List<ImportLine> importLines;
    private Integer countLinesErrors;

}
