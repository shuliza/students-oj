package com.studentoj.judge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studentoj.judge.entity.ProblemTestcaseEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ProblemTestcaseMapper extends BaseMapper<ProblemTestcaseEntity> {
    @Select("""
            SELECT id, problem_id, ordinal, init_sql
            FROM problem_testcase
            WHERE problem_id = #{problemId}
            ORDER BY ordinal ASC, id ASC
            """)
    List<ProblemTestcaseEntity> selectByProblem(@Param("problemId") Long problemId);
}
