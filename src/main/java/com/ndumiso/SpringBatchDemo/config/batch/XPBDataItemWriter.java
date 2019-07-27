package com.ndumiso.SpringBatchDemo.config.batch;

import com.ndumiso.SpringBatchDemo.domain.XPBStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * We can inject a JPA repository here, or a datasource and persist chunks to DB
 */
@Component
public class XPBDataItemWriter implements ItemWriter<XPBStatement> {

    private static final Logger LOG = LoggerFactory.getLogger(XPBDataItemWriter.class);

    @Override
    @Retryable
    public void write(List<? extends XPBStatement> items) throws Exception {
        LOG.info("Chunk size {}, Writing items to database : {} ", items.size(), items);
    }
}
