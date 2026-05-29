package com.studentoj.problem.dto;

import java.util.List;

public record ProblemResponse(
        Long id,
        String title,
        String difficulty,
        List<String> tags,
        Integer passRate,
        Integer submissions,
        String status,
        String description,
        String sampleInput,
        String sampleOutput
) {
}
