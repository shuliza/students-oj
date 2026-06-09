package com.studentoj.problem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studentoj.problem.entity.ProblemEntity;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ProblemMapper extends BaseMapper<ProblemEntity> {

    /**
     * 题目维度的统计数据：提交数、通过率(通过学生数/做过学生数)。
     */
    @Select("""
            SELECT p.id AS problem_id,
                   COUNT(DISTINCT s.user_id) AS submissions,
                   COALESCE(ROUND(COUNT(DISTINCT CASE WHEN s.status = 'ACCEPTED' THEN s.user_id END) * 100.0
                           / NULLIF(COUNT(DISTINCT s.user_id), 0)), 0) AS pass_rate
            FROM problem p
            LEFT JOIN submission s ON s.problem_id = p.id
            GROUP BY p.id
            """)
    List<Map<String, Object>> selectStats();

    /**
     * 当前用户对每道题的最佳状态：2 = ACCEPTED, 1 = 已尝试未通过, 不返回则未尝试。
     */
    @Select("""
            SELECT problem_id AS problem_id,
                   MAX(CASE WHEN status = 'ACCEPTED' THEN 2 ELSE 1 END) AS best_state
            FROM submission
            WHERE user_id = #{userId}
            GROUP BY problem_id
            """)
    List<Map<String, Object>> selectUserState(@Param("userId") Long userId);
}
