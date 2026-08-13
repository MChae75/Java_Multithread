package com.example.flashsale.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@EnableAsync
public class ExecutorConfig {

    @Bean
    public ExecutorService orderProcessingExecutor() {
        // Thread pool for parallel order processing (authentication, stock check, payment)
        return Executors.newFixedThreadPool(10);
    }

    @Bean
    public ScheduledExecutorService scheduledExecutor() {
        // Scheduled executor for periodic tasks like inventory sync
        return Executors.newScheduledThreadPool(5);
    }
}
