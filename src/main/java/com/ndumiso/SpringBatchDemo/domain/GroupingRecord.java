package com.ndumiso.SpringBatchDemo.domain;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class GroupingRecord {

    @NotNull
    private String statementNumber;

    @NotNull
    private Integer order;

    @NotNull
    private String serviceCode;

    @NotNull
    private String serviceDescription;

    @NotNull
    private String chargeCode;

    @NotNull
    private String chargeDescription;

    @NotNull
    private Integer totalNumberOfTransactions;

    @NotNull
    private String transactionCurrency;

    @NotNull
    private BigDecimal totalTransactionValue;

}
