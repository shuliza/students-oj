package com.studentoj.leetcodecrawler.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studentoj.leetcodecrawler.entity.SqlProblem;
import com.studentoj.leetcodecrawler.mapper.SqlProblemMapper;
import com.studentoj.leetcodecrawler.service.SqlProblemService;
import org.springframework.stereotype.Service;

@Service
public class SqlProblemServiceImpl extends ServiceImpl<SqlProblemMapper, SqlProblem> implements SqlProblemService {
}
