package com.ndumiso.SpringBatchDemo.config.batch;

import com.ndumiso.SpringBatchDemo.domain.XPBStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.MultiResourcePartitioner;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Configuration defining a Jobs , Steps, ItemReaders, Processors and Writers to use.
 *
 * @author Ndumiso
 */
@Configuration
@EnableBatchProcessing
public class BatchConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchConfig.class);

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    public BatchConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @StepScope
    @Bean
    public Partitioner partitioner() {
        LOGGER.info("************** building step partitioner ***************");
        MultiResourcePartitioner multiResourcePartitioner = new MultiResourcePartitioner();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = null;
        String LocationPatternClassPtah = "XPB_Statements/XPB_CashPlus_Stm_*_*.dat";
        String locationPatternWindows = "file:C:/integration/xpb_statements/XPB_CashPlus_Stm_*_*.dat";
        String locationPatternLinux = "file:/home/f5298334/Documents/xpb_statements/XPB_CashPlus_Stm_*_*.dat";
        try {
            resources = resolver.getResources(locationPatternLinux);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        multiResourcePartitioner.setResources(resources);
        multiResourcePartitioner.partition(10);
        return multiResourcePartitioner;
    }

    @Bean
    public Step masterStep() {
        return stepBuilderFactory.get("masterStep")
                .partitioner("step1", partitioner())
                .step(step1(null))
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(10);
        taskExecutor.setCorePoolSize(10);
        taskExecutor.setQueueCapacity(10);
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }

    @Bean
    @StepScope
    public XPBStatementFileReader statementItemReader(@Value("#{stepExecutionContext['fileName']}") String filename) {
        XPBStatementFileReader xpbStatementFileReader = new XPBStatementFileReader();
        try {
            xpbStatementFileReader.setResource(new UrlResource(filename));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return xpbStatementFileReader;
    }

    @Bean
    public Job importXPBStatements(JobExecutionListener listener, Step step1) {
        return jobBuilderFactory.get("importXPBStatements")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(masterStep())
                .build();
    }

    @Bean
    public XPBStatementProcessor statementProcessor() {
        return new XPBStatementProcessor();
    }


    @Bean
    public Step step1(XPBStatementWriter xpbDataWriter) {
        return stepBuilderFactory.get("step1 - read statements and write them to database")
                .<XPBStatement, XPBStatement>chunk(1000)
                .reader(statementItemReader(null))
                .faultTolerant()
                .processor(statementProcessor())
                .writer(xpbDataWriter)
                .startLimit(5)
                .build();

    }
}
