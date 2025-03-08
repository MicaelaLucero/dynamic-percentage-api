package com.tenpo.challenge.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@EnableRetry
public class RetryConfig {

    @Value("${retry.max-attempts}")
    private int maxAttempts;

    @Value("${retry.backoff-delay}")
    private long backoffDelay;

    @Bean
    public RetryTemplate retryTemplate() {
        return RetryTemplate.builder()
                .maxAttempts(maxAttempts)
                .fixedBackoff(backoffDelay)
                .build();
    }

    @Bean
    public RetryOperationsInterceptor retryInterceptor(RetryTemplate retryTemplate) {
        return RetryInterceptorBuilder.stateless()
                .retryOperations(retryTemplate)
                .build();
    }
}