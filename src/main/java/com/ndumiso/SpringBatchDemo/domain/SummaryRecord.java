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
public class SummaryRecord {

    @NotNull
    private String statementNumber;

    @NotNull
    private Integer order;

    @NotNull
    private String type;

    @NotNull
    private String currency;

    @NotNull //round to 2 decimal places
    private BigDecimal totalAmount;

}
