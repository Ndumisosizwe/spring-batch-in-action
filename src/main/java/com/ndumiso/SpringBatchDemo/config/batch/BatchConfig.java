package com.ndumiso.SpringBatchDemo.config.batch;

import com.ndumiso.SpringBatchDemo.domain.XPBStatement;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

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

    @Value("classpath:XPB_Statements/XPB_CashPlus_Stm_*_*.dat") //classpath
//    @Value("file:C:/integration/xpb_statements_data/XPB_CashPlus_Stm_*_*.dat") //in file system
    private Resource[] commissionStatementFiles;

    public BatchConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public MultiResourceItemReader<XPBStatement> multiResourceItemReader() throws IOException {
        MultiResourceItemReader<XPBStatement> resourceItemReader = new MultiResourceItemReader<>();
        resourceItemReader.setResources(commissionStatementFiles);
        resourceItemReader.setStrict(true);
        resourceItemReader.setDelegate(fileItemReader());
        return resourceItemReader;
    }

    @Bean
    public XPBStatementFileReader fileItemReader() {
        return new XPBStatementFileReader();
    }

    @Bean
    public Job importXPBDataJob(JobCompletionNotification listener, Step step1) {
        return jobBuilderFactory.get("importXPBData")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1)
                .end()
                .build();
    }

    @Bean
    public Step step1(XPBDataItemWriter xpbDataWriter) throws IOException {
        return stepBuilderFactory.get("step1 - read statements and write them to database")
                .<XPBStatement, XPBStatement>chunk(10)
                .reader(multiResourceItemReader())
//                .processor(productItemProcessor()) we can have a processor
                .writer(xpbDataWriter)
                .build();

    }
}
