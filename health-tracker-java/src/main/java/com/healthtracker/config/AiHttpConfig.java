package com.healthtracker.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AiHttpConfig {
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder,
                                     @Value("${ai.http.connect-timeout-seconds:10}") int connectTimeoutSeconds,
                                     @Value("${ai.http.read-timeout-seconds:60}") int readTimeoutSeconds) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
            .setReadTimeout(Duration.ofSeconds(readTimeoutSeconds))
            .build();
    }
}
