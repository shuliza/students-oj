package com.studentoj.leetcodecrawler.importer;

import com.studentoj.leetcodecrawler.client.LeetCodeClient;
import com.studentoj.leetcodecrawler.config.LeetCodeCrawlerProperties;
import com.studentoj.leetcodecrawler.dto.LeetCodeProblemDetail;
import com.studentoj.leetcodecrawler.dto.LeetCodeProblemSummary;
import com.studentoj.leetcodecrawler.dto.ParsedProblem;
import com.studentoj.leetcodecrawler.entity.SqlProblem;
import com.studentoj.leetcodecrawler.parser.ProblemParser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqlProblemImportService {
    private final LeetCodeClient leetCodeClient;
    private final ProblemParser problemParser;
    private final ProblemImporter problemImporter;
    private final LeetCodeCrawlerProperties properties;

    public SyncResult syncAll() {
        log.info("开始同步 LeetCode Database SQL 题目");
        List<LeetCodeProblemSummary> summaries = leetCodeClient.fetchDatabaseProblems();
        log.info("发现题目数量: {}", summaries.size());
        int inserted = 0;
        int updated = 0;
        int skipped = 0;
        int failed = 0;
        for (LeetCodeProblemSummary summary : summaries) {
            log.info("发现题目: {}", summary.title());
            try {
                LeetCodeProblemDetail detail = leetCodeClient.fetchProblemDetail(summary.titleSlug());
                ParsedProblem parsed = problemParser.parse(detail);
                log.info("解析成功: {}", detail.title());
                ProblemImporter.ImportResult result = problemImporter.upsert(toEntity(detail, parsed));
                if (result == ProblemImporter.ImportResult.INSERTED) {
                    inserted++;
                } else if (result == ProblemImporter.ImportResult.UPDATED) {
                    updated++;
                } else {
                    skipped++;
                }
            } catch (Exception ex) {
                failed++;
                log.error("失败原因: {} - {}", summary.titleSlug(), ex.getMessage(), ex);
            }
        }
        log.info("同步结束: inserted={}, updated={}, skipped={}, failed={}", inserted, updated, skipped, failed);
        return new SyncResult(summaries.size(), inserted, updated, skipped, failed);
    }

    public SyncResult syncIncremental() {
        log.info("开始增量同步 LeetCode Database SQL 题目，公开 GraphQL 未稳定暴露题目更新时间，使用 titleSlug 唯一键和内容差异跳过未变化题目");
        return syncAll();
    }

    private SqlProblem toEntity(LeetCodeProblemDetail detail, ParsedProblem parsed) {
        SqlProblem problem = new SqlProblem();
        problem.setTitle(detail.title());
        problem.setTitleSlug(detail.titleSlug());
        problem.setDifficulty(detail.difficulty());
        problem.setContent(detail.content());
        problem.setContentText(parsed.contentText());
        problem.setExample(parsed.example());
        problem.setSchemaInfo(parsed.schemaInfo());
        problem.setSampleData(parsed.sampleData());
        problem.setExpectedOutput(parsed.expectedOutput());
        problem.setTestCases(parsed.testCases());
        problem.setHint(String.join(System.lineSeparator(), detail.hints()));
        problem.setTags(String.join(",", detail.tags()));
        problem.setSource("LeetCode");
        problem.setSourceUrl(properties.baseUrl() + "/problems/" + detail.titleSlug() + "/");
        return problem;
    }

    public record SyncResult(int discovered, int inserted, int updated, int skipped, int failed) {
    }
}
