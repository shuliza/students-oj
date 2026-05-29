package com.studentoj.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studentoj.auth.entity.UserEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<UserEntity> {

    @Select("SELECT name FROM class_group WHERE id = #{groupId}")
    String selectGroupName(@Param("groupId") Long groupId);
}
