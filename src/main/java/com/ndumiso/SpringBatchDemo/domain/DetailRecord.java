package com.ndumiso.SpringBatchDemo.domain;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class DetailRecord {

    @NotNull
    private String statementNumber;

    @NotNull
    private Integer order;

    @NotNull
    private LocalDate transactionDate;

    @NotNull
    private String serviceCode;

    @NotNull
    private String serviceDescription;

    @NotNull
    private String chargeCode;

    @NotNull
    private String chargeDescription;

    @NotNull
    private String transactionCurrency;

    @NotNull
    private BigDecimal transactionAmount;

    @NotNull
    private String commissionCurrency;

    @NotNull
    private BigDecimal commissionAmount;
}
