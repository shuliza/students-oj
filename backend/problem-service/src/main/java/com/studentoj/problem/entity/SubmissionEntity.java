package com.studentoj.problem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("submission")
public class SubmissionEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long problemId;
    private String sqlContent;
    private String status;
    private Integer score;
    private Integer runtimeMs;
    private String message;
    private LocalDateTime submittedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getProblemId() { return problemId; }
    public void setProblemId(Long problemId) { this.problemId = problemId; }
    public String getSqlContent() { return sqlContent; }
    public void setSqlContent(String sqlContent) { this.sqlContent = sqlContent; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Integer getRuntimeMs() { return runtimeMs; }
    public void setRuntimeMs(Integer runtimeMs) { this.runtimeMs = runtimeMs; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}
