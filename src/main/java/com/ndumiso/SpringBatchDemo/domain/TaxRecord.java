package com.ndumiso.SpringBatchDemo.domain;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * XPB statement X-Record (Tax Record)
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class TaxRecord {

    @NotNull
    private String statementNumber;

    @NotNull
    private Integer order;

    @NotNull
    private String type;

    @NotNull
    private String currency;

    @NotNull
    private BigDecimal taxAmount;

    @NotNull
    private BigDecimal rate;

}
