package com.studentoj.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studentoj.ai.entity.AiSuggestionEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface AiSuggestionMapper extends BaseMapper<AiSuggestionEntity> {
    @Select("""
            SELECT id, user_id, submission_id, problem_id, suggestion, created_at
            FROM ai_suggestion
            WHERE user_id = #{userId}
              AND problem_id = #{problemId}
            ORDER BY created_at DESC
            LIMIT 5
            """)
    List<AiSuggestionEntity> selectLatestByUserAndProblem(@Param("userId") Long userId, @Param("problemId") Long problemId);
}
