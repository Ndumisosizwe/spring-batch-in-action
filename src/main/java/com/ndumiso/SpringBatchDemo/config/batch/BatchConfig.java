package com.ndumiso.SpringBatchDemo.config.batch;

import com.ndumiso.SpringBatchDemo.domain.XPBStatement;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Configuration defining a Jobs , Steps, ItemReaders, Processors and Writers to use.
 *
 * @author Ndumiso
 */
@Configuration
@EnableBatchProcessing
public class BatchConfig {


    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    public BatchConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public MultiResourceItemReader<XPBStatement> multiResourceStatementReader() throws IOException {
        MultiResourceItemReader<XPBStatement> resourceItemReader = new MultiResourceItemReader<>();
        resourceItemReader.setDelegate(statementItemReader());
        return resourceItemReader;
    }

    @Bean
    public XPBStatementFileReader statementItemReader() {
        return new XPBStatementFileReader();
    }

    @Bean
    public Job importXPBStatements(JobExecutionListener listener, Step step1) {
        return jobBuilderFactory.get("importXPBStatements")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(step1)
                .build();
    }

    @Bean
    public XPBStatementProcessor statementProcessor() {
        return new XPBStatementProcessor();
    }


    @Bean
    public Step step1(XPBStatementWriter xpbDataWriter) throws IOException {
        return stepBuilderFactory.get("step1 - read statements and write them to database")
                .<XPBStatement, XPBStatement>chunk(1000)
                .reader(multiResourceStatementReader())
                .faultTolerant()
                .processor(statementProcessor())
                .writer(xpbDataWriter)
                .startLimit(5)
                .build();

    }
}
