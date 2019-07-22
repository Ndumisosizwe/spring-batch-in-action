package com.ndumiso.SpringBatchDemo.config.batch;

import com.ndumiso.SpringBatchDemo.domain.Product;
import com.ndumiso.SpringBatchDemo.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * My ItemWriter, takes each chunk it receives and saves all to DB
 *
 * @author Ndumiso
 */
@Component
public class ProductItemWriter implements ItemWriter<Product> {

    private final ProductRepository productRepository;
    private static final Logger logger = LoggerFactory.getLogger(ProductItemWriter.class);

    public ProductItemWriter(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void write(List<? extends Product> items) throws Exception {
        logger.info("The chunk size is... " + items.size());
        logger.info("Processing chunk {}:", items);
        this.productRepository.saveAll(items);
    }
}
