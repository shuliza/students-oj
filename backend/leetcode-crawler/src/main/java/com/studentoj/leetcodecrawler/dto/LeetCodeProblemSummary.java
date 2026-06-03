package com.studentoj.leetcodecrawler.dto;

import java.util.List;

public record LeetCodeProblemSummary(
        String title,
        String titleSlug,
        String difficulty,
        List<String> tags,
        boolean paidOnly
) {
    public boolean isSqlProblem() {
        if (tags == null) {
            return false;
        }
        return tags.stream().anyMatch(tag ->
                "Database".equalsIgnoreCase(tag) || "SQL".equalsIgnoreCase(tag) || "数据库".equals(tag));
    }
}
