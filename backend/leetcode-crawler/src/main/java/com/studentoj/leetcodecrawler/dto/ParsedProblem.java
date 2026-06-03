package com.studentoj.leetcodecrawler.dto;

public record ParsedProblem(
        String contentText,
        String example,
        String schemaInfo,
        String sampleData,
        String expectedOutput,
        String testCases
) {
}
