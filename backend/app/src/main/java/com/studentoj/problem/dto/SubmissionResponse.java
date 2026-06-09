package com.studentoj.problem.dto;

public record SubmissionResponse(
        Long id,
        Long problemId,
        String problemTitle,
        Long userId,
        String userName,
        String status,
        Integer score,
        Integer runtimeMs,
        String submittedAt,
        String message
) {
}
