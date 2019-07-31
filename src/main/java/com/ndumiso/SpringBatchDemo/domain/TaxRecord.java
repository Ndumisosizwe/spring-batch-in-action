package com.ndumiso.SpringBatchDemo.domain;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@EqualsAndHashCode(callSuper = false)
@Builder
@Entity
public class TaxRecord extends BaseEntity {

    @NotNull
    private String statementNumber;

    @NotNull
    @Column(name = "sorting_order")
    private Integer order;

    @NotNull
    private String type;

    @NotNull
    private String currency;

    @NotNull
    private BigDecimal taxAmount;

    @NotNull
    private BigDecimal rate;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id")
    private XPBStatement xpbStatement;

}
