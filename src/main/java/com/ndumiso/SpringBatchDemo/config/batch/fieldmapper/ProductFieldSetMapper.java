package com.ndumiso.SpringBatchDemo.config.batch.fieldmapper;

import com.ndumiso.SpringBatchDemo.domain.Product;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

/**
 * @author Ndumiso
 * Responsible for mapping each row on the flat file (CSV in this case) to the Product POJO
 */
public class ProductFieldSetMapper implements FieldSetMapper<Product> {

    @Override
    public Product mapFieldSet(FieldSet fieldSet) {
        Product product = new Product();
        product.setDescription(fieldSet.readString("DESCRIPTION"));
        product.setProductId(fieldSet.readString("PRODUCT_ID"));
        product.setName(fieldSet.readString("NAME"));
        product.setPrice(fieldSet.readBigDecimal("PRICE"));
        return product;
    }
}
