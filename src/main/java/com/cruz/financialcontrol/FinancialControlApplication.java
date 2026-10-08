package com.cruz.financialcontrol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class FinancialControlApplication {

    static void main(String[] args) {
        SpringApplication.run(FinancialControlApplication.class, args);
    }
}
