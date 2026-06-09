package com.studentoj.problem.dto;

import java.util.List;

/**
 * 教师端题目详情：包含学生端不可见的 answerSql 与各测试用例 init_sql。
 */
public record ProblemAdminResponse(
        Long id,
        String title,
        String difficulty,
        List<String> tags,
        String description,
        String sampleInput,
        String sampleOutput,
        String answerSql,
        List<String> testcases,
        Integer status,
        Integer submissions,
        Integer passRate
) {
}
