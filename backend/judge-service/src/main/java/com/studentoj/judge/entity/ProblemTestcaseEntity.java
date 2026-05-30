package com.studentoj.judge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("problem_testcase")
public class ProblemTestcaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long problemId;
    private Integer ordinal;
    private String initSql;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProblemId() { return problemId; }
    public void setProblemId(Long problemId) { this.problemId = problemId; }
    public Integer getOrdinal() { return ordinal; }
    public void setOrdinal(Integer ordinal) { this.ordinal = ordinal; }
    public String getInitSql() { return initSql; }
    public void setInitSql(String initSql) { this.initSql = initSql; }
}
