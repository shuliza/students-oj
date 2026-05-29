package com.studentoj.ai.dto;

public record AiSuggestionRequest(
        Long submissionId,
        Long problemId,
        String title,
        String sqlContent,
        String judgeStatus,
        String status,
        String errorMessage,
        String studentSql
) {
}
