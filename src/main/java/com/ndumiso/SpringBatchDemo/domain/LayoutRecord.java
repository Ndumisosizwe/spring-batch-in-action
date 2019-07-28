package com.ndumiso.SpringBatchDemo.domain;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * XPB statement layout record
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class LayoutRecord {

    @NotNull
    //  @OneToOne
    //  @JoinColumn(name = "statementNumber")
    private String statementNumber;   //TODO : Need to Implement DB relationships later

    @NotNull
    private LocalDate statementStartPeriod;

    @NotNull
    private LocalDate statementEndPeriod;

    @NotNull
    private LocalDate statementDate;

    /**
     * Cashplus DDAVA account number
     */
    @NotNull
    private String accountNumber;
}
