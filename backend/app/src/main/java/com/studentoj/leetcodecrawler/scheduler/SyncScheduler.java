package com.studentoj.leetcodecrawler.scheduler;

import com.studentoj.leetcodecrawler.config.LeetCodeCrawlerProperties;
import com.studentoj.leetcodecrawler.importer.SqlProblemImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncScheduler implements ApplicationRunner {
    private final SqlProblemImportService importService;
    private final LeetCodeCrawlerProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        if (properties.enabled() && properties.syncOnStartup()) {
            importService.syncIncremental();
        }
    }

    @Scheduled(cron = "${leetcode.crawler.scheduled-cron:0 0 2 * * ?}")
    public void scheduledSync() {
        if (!properties.enabled()) {
            log.info("LeetCode crawler disabled, skip scheduled sync");
            return;
        }
        importService.syncIncremental();
    }
}
