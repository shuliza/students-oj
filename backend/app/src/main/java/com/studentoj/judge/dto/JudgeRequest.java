package com.studentoj.judge.dto;

public record JudgeRequest(Long submissionId, Long userId, Long problemId, String sqlContent) {
}
