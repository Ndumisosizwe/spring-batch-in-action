package com.ndumiso.SpringBatchDemo.config.batch;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * A notification object invoked at the end of a job execution.
 *
 * @author Ndumiso
 */
@Component
public class JobCompletionNotification extends JobExecutionListenerSupport implements ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(JobCompletionNotification.class);
    private ApplicationContext applicationContext;

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            LOG.info("JOB " + jobExecution.getJobId() + " FINISHED! " + jobExecution.getJobInstance().getJobName());
            MultiResourceItemReader reader = (MultiResourceItemReader) applicationContext.getBean("multiResourceItemReader");
            reader.close();
        }
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        MultiResourceItemReader reader = (MultiResourceItemReader) applicationContext.getBean("multiResourceItemReader");
        try {
            reader.setResources(applicationContext.getResources("file:C:/integration/xpb_statements_data/XPB_CashPlus_Stm_*_*.dat"));
        } catch (IOException ex) {
            LOG.error("Unable to set file resources to bean multiResourceItemReader", ex);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
