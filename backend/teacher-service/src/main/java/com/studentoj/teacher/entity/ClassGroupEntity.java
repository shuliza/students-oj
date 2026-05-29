package com.studentoj.teacher.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("class_group")
public class ClassGroupEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private Long ownerTeacherId;
    private Integer status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getOwnerTeacherId() { return ownerTeacherId; }
    public void setOwnerTeacherId(Long ownerTeacherId) { this.ownerTeacherId = ownerTeacherId; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
