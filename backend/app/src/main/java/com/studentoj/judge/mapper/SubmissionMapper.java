package com.studentoj.judge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studentoj.judge.entity.SubmissionEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface SubmissionMapper extends BaseMapper<SubmissionEntity> {

    @Update("UPDATE submission SET status = #{status}, score = #{score}, "
            + "runtime_ms = #{runtimeMs}, message = #{message} WHERE id = #{id}")
    int updateResult(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("score") int score,
                     @Param("runtimeMs") int runtimeMs,
                     @Param("message") String message);
}
