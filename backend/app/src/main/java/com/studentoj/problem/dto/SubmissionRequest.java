package com.studentoj.problem.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record SubmissionRequest(Long userId, Long problemId, @JsonAlias("sql") String sqlContent) {
}
