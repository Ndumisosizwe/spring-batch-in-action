package com.ndumiso.SpringBatchDemo.domain;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * XPB sends us a subset of data with many DDAVA statements in a "|" delimited file format.
 */

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class XPBStatement {

    @NotNull
    private String productionDate;

    @NotNull
    private String fileName;

    @NotNull
    @EqualsAndHashCode.Include
//    @OneToOne
    private LayoutRecord layoutRecord;

    @NotNull
    private Set<SummaryRecord> summaryRecords;

    @NotNull
    private Set<TaxRecord> taxRecords;

    @NotNull
    private Set<GroupingRecord> groupingRecords;

    @NotNull
    private Set<OtherRecord> otherRecords;

    @NotNull
    private Set<DetailRecord> detailRecords;
}
