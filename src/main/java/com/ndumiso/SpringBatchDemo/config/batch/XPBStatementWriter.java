package com.ndumiso.SpringBatchDemo.config.batch;

import com.ndumiso.SpringBatchDemo.domain.XPBStatement;
import com.ndumiso.SpringBatchDemo.repository.XPBStatementRepository;
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
public class XPBStatementWriter implements ItemWriter<XPBStatement> {

    private static final Logger LOG = LoggerFactory.getLogger(XPBStatementWriter.class);
    private final XPBStatementRepository statementRepository;

    public XPBStatementWriter(XPBStatementRepository statementRepository) {
        this.statementRepository = statementRepository;
    }

    @Override
    @Retryable
    public void write(List<? extends XPBStatement> items) throws Exception {
        items.forEach(i -> i.setStatementNumber(i.getStatementNumber().concat("_"+ Thread.currentThread().getName())));
        LOG.info("Chunk size {}, Writing items to database : {} ", items.size(), items);
        statementRepository.saveAll(items);
    }
}
