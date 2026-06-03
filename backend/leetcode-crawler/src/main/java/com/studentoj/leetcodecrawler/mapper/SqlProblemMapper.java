package com.studentoj.leetcodecrawler.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studentoj.leetcodecrawler.entity.SqlProblem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SqlProblemMapper extends BaseMapper<SqlProblem> {
}
