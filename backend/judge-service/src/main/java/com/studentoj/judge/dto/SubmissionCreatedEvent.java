package com.studentoj.judge.dto;

public record SubmissionCreatedEvent(Long submissionId, Long userId, Long problemId, String sqlContent) {
}
