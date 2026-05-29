package com.studentoj.teacher.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("group_user_relation")
public class GroupUserRelationEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long groupId;
    private Long userId;
    private String roleInGroup;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getRoleInGroup() { return roleInGroup; }
    public void setRoleInGroup(String roleInGroup) { this.roleInGroup = roleInGroup; }
}
