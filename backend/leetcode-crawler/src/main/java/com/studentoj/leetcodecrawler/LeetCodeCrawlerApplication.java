package com.studentoj.leetcodecrawler;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableRetry
@EnableScheduling
@MapperScan("com.studentoj.leetcodecrawler.mapper")
@ConfigurationPropertiesScan
@SpringBootApplication
public class LeetCodeCrawlerApplication {
    public static void main(String[] args) {
        SpringApplication.run(LeetCodeCrawlerApplication.class, args);
    }
}
