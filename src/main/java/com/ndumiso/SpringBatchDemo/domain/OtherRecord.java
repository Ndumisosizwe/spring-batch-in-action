package com.ndumiso.SpringBatchDemo.domain;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * XPB Other (O record)
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Builder
@Entity
public class OtherRecord extends BaseEntity{

    @NotNull
    private String statementNumber;

    @NotNull
    @Column(name = "sorting_order")
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
    private String currency;

    @NotNull
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id")
    private XPBStatement xpbStatement;


}
