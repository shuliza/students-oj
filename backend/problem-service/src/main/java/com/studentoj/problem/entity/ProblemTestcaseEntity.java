package com.studentoj.problem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("problem_testcase")
public class ProblemTestcaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long problemId;
    private Integer ordinal;
    private String initSql;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProblemId() { return problemId; }
    public void setProblemId(Long problemId) { this.problemId = problemId; }
    public Integer getOrdinal() { return ordinal; }
    public void setOrdinal(Integer ordinal) { this.ordinal = ordinal; }
    public String getInitSql() { return initSql; }
    public void setInitSql(String initSql) { this.initSql = initSql; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
