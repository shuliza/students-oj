package com.studentoj.ai.service;

import com.studentoj.ai.dto.AiSuggestionRequest;
import org.springframework.stereotype.Component;

@Component
public class RuleSuggestionGenerator {

    public String generate(AiSuggestionRequest request) {
        String status = firstNonBlank(request.status(), request.judgeStatus()).toUpperCase();
        String sql = firstNonBlank(request.studentSql(), request.sqlContent()).toLowerCase();
        String error = firstNonBlank(request.errorMessage(), "");

        if ("ACCEPTED".equals(status)) {
            return "SQL 已通过。进阶建议：使用 EXPLAIN 观察执行计划，优先检查 JOIN 字段和 WHERE 过滤字段是否适合建立索引；同时保持列名别名清晰，提升可读性。";
        }
        if ("WRONG_ANSWER".equals(status) || "WA".equals(status)) {
            if (sql.contains("avg(") && !sql.contains("group by")) {
                return "结果不正确。你使用了聚合函数，但 SQL 中缺少 GROUP BY，请确认是否需要按课程、学生或题目要求的维度分组。";
            }
            if (sql.contains("order by")) {
                return "结果不正确。请检查 ORDER BY 的排序字段和升降序是否与题目要求一致，部分题目会严格比较结果顺序。";
            }
            if (sql.contains("join") && !sql.contains(" on ")) {
                return "结果不正确。JOIN 语句缺少明确的 ON 条件，请检查两张表之间的关联字段是否正确。";
            }
            if (!sql.contains("join") && (sql.contains("student") || sql.contains("score") || sql.contains("enroll"))) {
                return "结果不正确。请确认题目是否需要多表连接，重点检查表名、字段名、连接条件和筛选条件。";
            }
            return "结果不正确。建议逐项核对 SELECT 输出列、WHERE 条件、GROUP BY 分组、ORDER BY 排序以及题目要求的别名。";
        }
        if ("RUNTIME_ERROR".equals(status) || "RE".equals(status)) {
            if (!error.isBlank()) {
                return "SQL 执行异常。错误信息：" + error + "。请优先检查该位置附近的关键字、括号、逗号、表名和字段名。";
            }
            return "SQL 执行异常。请检查语法结构、表名字段名是否存在，以及函数和窗口语句的写法是否被当前 MySQL 版本支持。";
        }
        if ("TIME_LIMIT".equals(status) || "TLE".equals(status)) {
            return "SQL 执行超时。建议减少无条件笛卡尔积，补充 JOIN 条件，避免在大结果集上做不必要的排序或重复子查询。";
        }
        return "建议检查表名、字段名、连接条件、筛选条件和结果列别名；如果仍不通过，可先用小样例手工推导期望输出，再对照 SQL 结果。";
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first.trim();
        }
        return second == null ? "" : second.trim();
    }
}
