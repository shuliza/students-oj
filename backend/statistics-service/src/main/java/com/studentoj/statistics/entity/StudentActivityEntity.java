package com.studentoj.statistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;

@TableName("student_activity")
public class StudentActivityEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDate activityDate;
    private Integer submissionCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public LocalDate getActivityDate() { return activityDate; }
    public void setActivityDate(LocalDate activityDate) { this.activityDate = activityDate; }
    public Integer getSubmissionCount() { return submissionCount; }
    public void setSubmissionCount(Integer submissionCount) { this.submissionCount = submissionCount; }
}
