package com.studentoj.leetcodecrawler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "leetcode.crawler")
public record LeetCodeCrawlerProperties(
        boolean enabled,
        String graphqlUrl,
        String baseUrl,
        String categorySlug,
        int pageSize,
        long requestMinDelayMs,
        long requestMaxDelayMs,
        String userAgent,
        String cookie,
        String scheduledCron,
        boolean syncOnStartup
) {
}
