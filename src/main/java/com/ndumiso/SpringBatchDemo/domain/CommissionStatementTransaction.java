package com.ndumiso.SpringBatchDemo.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionStatementTransaction {

    private LocalDateTime date;

    private String description;

    private String reference;

    private BigDecimal amount;

    private BigDecimal commission;

    private TransactionType transactionType;

    // and so on ....
}
