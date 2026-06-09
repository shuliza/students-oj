package com.studentoj.leetcodecrawler.importer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studentoj.leetcodecrawler.entity.SqlProblem;
import com.studentoj.leetcodecrawler.service.SqlProblemService;
import java.time.LocalDateTime;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemImporter {
    private final SqlProblemService sqlProblemService;

    @Transactional
    public ImportResult upsert(SqlProblem problem) {
        if (!StringUtils.hasText(problem.getTitleSlug())) {
            throw new IllegalArgumentException("titleSlug must not be blank");
        }
        LocalDateTime now = LocalDateTime.now();
        SqlProblem existing = sqlProblemService.getOne(new LambdaQueryWrapper<SqlProblem>()
                .eq(SqlProblem::getTitleSlug, problem.getTitleSlug())
                .last("LIMIT 1"));
        if (existing == null) {
            problem.setCreateTime(now);
            problem.setUpdateTime(now);
            sqlProblemService.save(problem);
            log.info("导入成功: {}", problem.getTitle());
            return ImportResult.INSERTED;
        }
        if (sameProblem(existing, problem)) {
            log.info("题目未变化，跳过更新: {}", problem.getTitle());
            return ImportResult.SKIPPED;
        }
        problem.setId(existing.getId());
        problem.setCreateTime(existing.getCreateTime());
        problem.setUpdateTime(now);
        sqlProblemService.updateById(problem);
        log.info("更新成功: {}", problem.getTitle());
        return ImportResult.UPDATED;
    }

    private boolean sameProblem(SqlProblem existing, SqlProblem incoming) {
        return equals(existing.getTitle(), incoming.getTitle())
                && equals(existing.getDifficulty(), incoming.getDifficulty())
                && equals(existing.getContent(), incoming.getContent())
                && equals(existing.getContentText(), incoming.getContentText())
                && equals(existing.getExample(), incoming.getExample())
                && equals(existing.getSchemaInfo(), incoming.getSchemaInfo())
                && equals(existing.getSampleData(), incoming.getSampleData())
                && equals(existing.getExpectedOutput(), incoming.getExpectedOutput())
                && equals(existing.getTestCases(), incoming.getTestCases())
                && equals(existing.getHint(), incoming.getHint())
                && equals(existing.getTags(), incoming.getTags())
                && equals(existing.getSourceUrl(), incoming.getSourceUrl());
    }

    private boolean equals(String left, String right) {
        return left == null ? right == null : left.equals(right);
    }

    @Transactional
    public BatchImportResult upsertBatch(Collection<SqlProblem> problems) {
        int inserted = 0;
        int updated = 0;
        int skipped = 0;
        for (SqlProblem problem : problems) {
            ImportResult result = upsert(problem);
            if (result == ImportResult.INSERTED) {
                inserted++;
            } else if (result == ImportResult.UPDATED) {
                updated++;
            } else {
                skipped++;
            }
        }
        return new BatchImportResult(inserted, updated, skipped);
    }

    public enum ImportResult {
        INSERTED,
        UPDATED,
        SKIPPED
    }

    public record BatchImportResult(int inserted, int updated, int skipped) {
    }
}
