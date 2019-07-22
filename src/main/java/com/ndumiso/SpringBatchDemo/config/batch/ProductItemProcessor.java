package com.ndumiso.SpringBatchDemo.config.batch;

import com.ndumiso.SpringBatchDemo.domain.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

/**
 * An Item processor, all i do here is for each product i change their name to UpperCase
 *
 * @author Ndumiso
 */
public class ProductItemProcessor implements ItemProcessor<Product, Product> {

    private static final Logger logger = LoggerFactory.getLogger(ProductItemProcessor.class);

    @Override
    public Product process(Product item) throws Exception {
        logger.info("Mocking item processing...");
        item.setName(item.getName().toUpperCase());
        return item;
    }
}
