package com.ndumiso.SpringBatchDemo.config.batch;

import com.ndumiso.SpringBatchDemo.domain.XPBStatement;
import com.ndumiso.SpringBatchDemo.repository.XPBStatementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
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
    public void write(List<? extends XPBStatement> items) throws Exception {
        LOG.info("writing statements {}", items);
        statementRepository.saveAll(items);
    }
}
