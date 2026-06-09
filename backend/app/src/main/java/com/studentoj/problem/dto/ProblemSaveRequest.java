package com.studentoj.problem.dto;

import java.util.List;

/**
 * 教师端题目保存请求。testcases 为多个数据集（每个是一段建表+造数据的 init_sql），
 * answerSql 为唯一参考查询；判题时对每个数据集分别比对学生 SQL 与参考 SQL 的结果。
 */
public record ProblemSaveRequest(
        String title,
        String difficulty,
        List<String> tags,
        String description,
        String sampleInput,
        String sampleOutput,
        String answerSql,
        List<String> testcases,
        Integer status
) {
}
