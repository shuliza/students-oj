package com.studentoj.problem.dto;

public record SubmissionRequest(Long userId, Long problemId, String sqlContent) {
}
