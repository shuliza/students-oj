package com.studentoj.problem.dto;

public record JudgeFinishedEvent(
        Long submissionId,
        Long userId,
        Long problemId,
        String status,
        Integer score,
        Integer runtimeMs,
        String message
) {
}
