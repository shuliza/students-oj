package com.studentoj.leetcodecrawler.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("sql_problem")
public class SqlProblem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String titleSlug;
    private String difficulty;
    private String content;
    private String contentText;
    private String example;
    private String schemaInfo;
    private String sampleData;
    private String expectedOutput;
    private String testCases;
    private String hint;
    private String tags;
    private String source;
    private String sourceUrl;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
