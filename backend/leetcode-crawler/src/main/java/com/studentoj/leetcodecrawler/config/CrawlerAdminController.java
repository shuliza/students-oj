package com.studentoj.leetcodecrawler.config;

import com.studentoj.leetcodecrawler.importer.SqlProblemImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/crawler/leetcode")
@RequiredArgsConstructor
public class CrawlerAdminController {
    private final SqlProblemImportService importService;

    @PostMapping("/sync/full")
    public SqlProblemImportService.SyncResult syncFull() {
        return importService.syncAll();
    }

    @PostMapping("/sync/incremental")
    public SqlProblemImportService.SyncResult syncIncremental() {
        return importService.syncIncremental();
    }
}
