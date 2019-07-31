package com.ndumiso.SpringBatchDemo.config.batch;

import com.ndumiso.SpringBatchDemo.domain.XPBStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class XPBStatementProcessor implements ItemProcessor<XPBStatement, XPBStatement> {

    private Logger LOGGER = LoggerFactory.getLogger(XPBStatementProcessor.class);

    @Override
    public XPBStatement process(XPBStatement item) throws Exception {
        LOGGER.info("Processing statement " + item);
        Thread.sleep(5_000);
        return item;
    }
}
