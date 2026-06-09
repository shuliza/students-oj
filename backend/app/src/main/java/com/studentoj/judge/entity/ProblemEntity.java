package com.studentoj.judge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("problem")
public class ProblemEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String initSql;
    private String answerSql;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getInitSql() { return initSql; }
    public void setInitSql(String initSql) { this.initSql = initSql; }
    public String getAnswerSql() { return answerSql; }
    public void setAnswerSql(String answerSql) { this.answerSql = answerSql; }
}
