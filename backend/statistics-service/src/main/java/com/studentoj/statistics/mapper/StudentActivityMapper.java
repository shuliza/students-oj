package com.studentoj.statistics.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studentoj.statistics.entity.StudentActivityEntity;
import java.time.LocalDate;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface StudentActivityMapper extends BaseMapper<StudentActivityEntity> {

    @Update("INSERT INTO student_activity(user_id, activity_date, submission_count) VALUES(#{userId}, #{date}, 1) " +
            "ON DUPLICATE KEY UPDATE submission_count = submission_count + 1")
    int upsertIncrement(@Param("userId") Long userId, @Param("date") LocalDate date);

    default StudentActivityEntity findByUserAndDate(Long userId, LocalDate date) {
        return selectOne(new QueryWrapper<StudentActivityEntity>()
                .eq("user_id", userId)
                .eq("activity_date", date)
                .last("LIMIT 1"));
    }
}
