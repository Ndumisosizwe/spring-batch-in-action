package com.ndumiso.SpringBatchDemo.config.batch;

import com.ndumiso.SpringBatchDemo.config.batch.fieldmapper.ProductFieldSetMapper;
import com.ndumiso.SpringBatchDemo.domain.Product;
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


    public final JobBuilderFactory jobBuilderFactory;

    public final StepBuilderFactory stepBuilderFactory;

    public BatchConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public FlatFileItemReader<Product> productReader() {
        FlatFileItemReader<Product> flatFileProductReader = new FlatFileItemReader<>();
        flatFileProductReader.setResource(new ClassPathResource("MOCK_PRODUCTS.csv"));
        flatFileProductReader.setLinesToSkip(1); // skip the first line of the CVS since it's the HEADER line.

        DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer(); // uses comma as default delimiter
        delimitedLineTokenizer.setNames("PRODUCT_ID", "NAME", "DESCRIPTION", "PRICE");

        DefaultLineMapper<Product> productLineMapper = new DefaultLineMapper<>();
        productLineMapper.setLineTokenizer(delimitedLineTokenizer);
        productLineMapper.setFieldSetMapper(new ProductFieldSetMapper());

        flatFileProductReader.setLineMapper(productLineMapper);
        return flatFileProductReader;
    }

    @Bean
    public ProductItemProcessor productItemProcessor() {
        return new ProductItemProcessor();
    }

    @Bean
    public Job importProductJob(JobCompletionNotification listener, Step step1) {
        return jobBuilderFactory.get("importProductJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1)
                .end()
                .build();
    }

    @Bean
    public Step step1(ProductItemWriter writer) {
        return stepBuilderFactory.get("step1")
                .<Product, Product>chunk(10)
                .reader(productReader())
                .processor(productItemProcessor())
                .writer(writer)
                .build();

    }
}
