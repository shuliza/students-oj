package com.studentoj.problem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studentoj.problem.entity.SubmissionEntity;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SubmissionMapper extends BaseMapper<SubmissionEntity> {

    @Select("""
            SELECT s.id AS id,
                   s.user_id AS user_id,
                   s.problem_id AS problem_id,
                   s.status AS status,
                   s.score AS score,
                   s.runtime_ms AS runtime_ms,
                   s.message AS message,
                   s.submitted_at AS submitted_at,
                   p.title AS problem_title,
                   u.real_name AS user_name
            FROM submission s
            LEFT JOIN problem p ON p.id = s.problem_id
            LEFT JOIN user u ON u.id = s.user_id
            LEFT JOIN class_group g ON g.id = u.group_id
            WHERE (#{studentId} IS NULL OR s.user_id = #{studentId})
              AND (#{groupName} IS NULL OR g.name = #{groupName})
            ORDER BY s.submitted_at DESC
            LIMIT 100
            """)
    List<Map<String, Object>> selectRecent(
            @Param("groupName") String groupName,
            @Param("studentId") Long studentId);

    @Select("""
            SELECT s.id AS id,
                   s.user_id AS user_id,
                   s.problem_id AS problem_id,
                   s.status AS status,
                   s.score AS score,
                   s.runtime_ms AS runtime_ms,
                   s.message AS message,
                   s.submitted_at AS submitted_at,
                   p.title AS problem_title,
                   u.real_name AS user_name
            FROM submission s
            LEFT JOIN problem p ON p.id = s.problem_id
            LEFT JOIN user u ON u.id = s.user_id
            WHERE s.user_id = #{userId}
            ORDER BY s.submitted_at DESC
            LIMIT 100
            """)
    List<Map<String, Object>> selectByUser(@Param("userId") Long userId);
}
