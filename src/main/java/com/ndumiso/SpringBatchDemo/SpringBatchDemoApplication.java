package com.ndumiso.SpringBatchDemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(value = "com.ndumiso.SpringBatchDemo.*")
public class SpringBatchDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchDemoApplication.class, args);
    }

}
