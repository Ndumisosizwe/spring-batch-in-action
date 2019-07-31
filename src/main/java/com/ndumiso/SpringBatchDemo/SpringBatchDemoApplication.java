package com.ndumiso.SpringBatchDemo;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableScheduling
public class SpringBatchDemoApplication {

    private final Job job;
    private final JobLauncher jobLauncher;

    public SpringBatchDemoApplication(JobLauncher jobLauncher, @Qualifier("importXPBStatements") Job job) {
        this.jobLauncher = jobLauncher;
        this.job = job;
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchDemoApplication.class, args);
    }

    @Scheduled(fixedRate = 2_000)
    public void perform() throws Exception {
        this.jobLauncher.run(job, new JobParametersBuilder().
                addLong("time", System.currentTimeMillis())
                .toJobParameters()
        );
    }

}
