package com.Ejada.BFF.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${user-service.url}")
    private String userServiceUrl;

    @Value("${account-service.url}")
    private String accountServiceUrl;

    @Value("${transaction-service.url}")
    private String transactionServiceUrl;

    @Bean
    public WebClient userServiceWebClient() {
        return WebClient.builder()
                .baseUrl(userServiceUrl)
                .build();
    }

    @Bean
    public WebClient accountServiceWebClient() {
        return WebClient.builder()
                .baseUrl(accountServiceUrl)
                .build();
    }

    @Bean
    public WebClient transactionServiceWebClient() {
        return WebClient.builder()
                .baseUrl(transactionServiceUrl)
                .build();
    }
}