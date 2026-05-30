package com.studentoj.ai.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AiClientConfig {

    @Bean
    RestTemplate aiRestTemplate(RestTemplateBuilder builder,
                                @Value("${studentoj.ai.deepseek.timeout-seconds:20}") int timeoutSeconds) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }
}
