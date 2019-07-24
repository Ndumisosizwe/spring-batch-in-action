package com.ndumiso.SpringBatchDemo.domain;

import lombok.*;

import java.math.BigDecimal;

/**
 * XPB sends us a subset of data related to a statement/client
 */

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class XPBCommissionStatementData {

    @EqualsAndHashCode.Include
    private String ucn;

    @EqualsAndHashCode.Include
    private String statementNumber;

    private BigDecimal totalVat;

    private BigDecimal totalCommissionExclVat;

    private BigDecimal totalCommissionInclVat;

    private BigDecimal withholdingTax;

//    @EqualsAndHashCode.Include
//    private Set<CommissionStatementTransaction> transactions;

    // and so on...

}
