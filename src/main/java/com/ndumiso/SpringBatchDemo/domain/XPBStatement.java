package com.ndumiso.SpringBatchDemo.domain;

import lombok.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;

/**
 * Abstraction of a XPB statement. XPB sends us a subset of data with many statements in a "|" delimited file format. Statements
 * are linked to DDAVA account numbers.
 */

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Builder
@Entity
public class XPBStatement extends BaseEntity {

    @NotNull
    private String productionDate;

    @NotNull
    private String fileName;

    //------- Layout Record Begin
    @NotNull
    @EqualsAndHashCode.Include
    private String statementNumber;

    @NotNull
    @EqualsAndHashCode.Include
    private LocalDate statementStartPeriod;

    @NotNull
    @EqualsAndHashCode.Include
    private LocalDate statementEndPeriod;

    @NotNull
    @EqualsAndHashCode.Include
    private LocalDate statementDate;

    @NotNull
    @EqualsAndHashCode.Include
    private String accountNumber;

    @NotNull
    @EqualsAndHashCode.Include
    private String uniqueCustomerNumber;
    //------- Layout Record End

    @NotNull
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "xpbStatement")
    private Set<SummaryRecord> summaryRecords;

    @NotNull
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "xpbStatement")
    private Set<TaxRecord> taxRecords;

    @NotNull
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "xpbStatement")
    private Set<GroupingRecord> groupingRecords;

    @NotNull
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "xpbStatement")
    private Set<OtherRecord> otherRecords;

    @NotNull
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "xpbStatement")
    private Set<DetailRecord> detailRecords;
}
