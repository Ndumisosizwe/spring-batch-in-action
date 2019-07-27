package com.ndumiso.SpringBatchDemo.domain;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.Set;

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
public class XPBStatement {

    @NotNull
    private String productionDate;

    @NotNull
    private String fileName;

    @NotNull
    @EqualsAndHashCode.Include
    private LayoutRecord layoutRecord;

    @NotNull
    private Set<SummaryRecord> summaryRecords;

    @NotNull
    private Set<TaxRecord> taxRecords;

    @NotNull
    private Set<GroupingRecord> groupingRecords;

}
