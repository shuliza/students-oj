package com.studentoj.ai.dto;

public record AiSuggestionResponse(
        Long id,
        Long userId,
        Long submissionId,
        Long problemId,
        String suggestion,
        String createdAt
) {
}
