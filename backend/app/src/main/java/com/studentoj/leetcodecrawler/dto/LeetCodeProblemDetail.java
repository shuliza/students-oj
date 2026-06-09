package com.studentoj.leetcodecrawler.dto;

import java.util.List;

public record LeetCodeProblemDetail(
        String title,
        String titleSlug,
        String difficulty,
        String content,
        List<String> tags,
        List<String> hints,
        List<CodeSnippet> codeSnippets,
        String mysqlSchemas
) {
    public record CodeSnippet(String lang, String langSlug, String code) {
    }
}
