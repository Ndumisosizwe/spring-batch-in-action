package com.ndumiso.SpringBatchDemo.config.batch.fieldmapper;

import com.ndumiso.SpringBatchDemo.domain.XPBCommissionStatementData;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class XPBCommissionStatementFieldMapper implements FieldSetMapper<XPBCommissionStatementData> {

    @Override
    public XPBCommissionStatementData mapFieldSet(FieldSet fieldSet) throws BindException {
        return XPBCommissionStatementData.builder()
                .ucn(fieldSet.readString("ucn"))
                .statementNumber(fieldSet.readString("statementNumber"))
                .totalCommissionExclVat(fieldSet.readBigDecimal("totalCommissionExclVat"))
                .totalCommissionInclVat(fieldSet.readBigDecimal("totalCommissionInclVat"))
                .withholdingTax(fieldSet.readBigDecimal("withholdingTax"))
                .totalVat(fieldSet.readBigDecimal("totalVat"))
                .build();
    }
}
