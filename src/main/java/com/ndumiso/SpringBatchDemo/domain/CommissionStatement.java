package com.ndumiso.SpringBatchDemo.domain;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class CommissionStatement extends XPBCommissionStatementData {

    private String clientBusinessName;

    private String businessAccountNumber;

    @EqualsAndHashCode.Include
    private LocalDate statementDate;

    private LocalDate dateFrom;

    private LocalDate dateTo;

    private String bankVatRegistrationNumber;

    // and so on ....

}
