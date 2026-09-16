package dev.rudyevhenii.crypto_aggregator.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class VirtualExecutorServiceConfig {

    @Bean
    public ExecutorService virtualExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
