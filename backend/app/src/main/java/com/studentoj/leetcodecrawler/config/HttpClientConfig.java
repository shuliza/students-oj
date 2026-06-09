package com.studentoj.leetcodecrawler.config;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientConfig {
    @Bean
    RestClient leetCodeRestClient(LeetCodeCrawlerProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(30));
        return RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(properties.graphqlUrl())
                .defaultHeader("User-Agent", properties.userAgent())
                .defaultHeader("Referer", properties.baseUrl() + "/problemset/database/")
                .defaultHeader("Origin", properties.baseUrl())
                .build();
    }
}
