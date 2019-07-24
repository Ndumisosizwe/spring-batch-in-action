package com.ndumiso.SpringBatchDemo.config.batch;

import com.ndumiso.SpringBatchDemo.config.batch.fieldmapper.XPBCommissionStatementFieldMapper;
import com.ndumiso.SpringBatchDemo.domain.XPBCommissionStatementData;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

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

    private static final String[] xpbStatementFields = new String[]{"ucn", "statementNumber", "totalCommissionExclVat", "totalCommissionInclVat", "withholdingTax", "totalVat"};

    public BatchConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }


    @Bean
    public FlatFileItemReader<XPBCommissionStatementData> XPBStatementsReader() {
        FlatFileItemReader<XPBCommissionStatementData> flatFileProductReader = new FlatFileItemReader<>();
        flatFileProductReader.setResource(new ClassPathResource("XPB_STATEMENTS_MOCK.csv"));
        flatFileProductReader.setLinesToSkip(1);
        DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
        delimitedLineTokenizer.setDelimiter("|");
        delimitedLineTokenizer.setNames(xpbStatementFields);

        DefaultLineMapper<XPBCommissionStatementData> commStatementLineMapper = new DefaultLineMapper<>();
        commStatementLineMapper.setLineTokenizer(delimitedLineTokenizer);
        commStatementLineMapper.setFieldSetMapper(new XPBCommissionStatementFieldMapper());

        flatFileProductReader.setLineMapper(commStatementLineMapper);
        return flatFileProductReader;
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
    public Step step1(XPBDataItemWriter xpbDataWriter) {
        return stepBuilderFactory.get("step1 - read rows from XPB CSV file & and write each POJO to database")
                .<XPBCommissionStatementData, XPBCommissionStatementData>chunk(100)
                .reader(XPBStatementsReader())
//                .processor(productItemProcessor()) we can have a processor
                .writer(xpbDataWriter)
                .build();

    }
}
