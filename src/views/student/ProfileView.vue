<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
</script>

<template>
  <div class="page">
    <div>
      <h1 class="page-title">个人中心</h1>
      <p class="page-subtitle">账号信息、班级与学习状态。</p>
    </div>
    <el-card class="section-card">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="姓名">{{ auth.user?.realName }}</el-descriptions-item>
        <el-descriptions-item label="账号">{{ auth.user?.username }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ auth.user?.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ auth.user?.groupName }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ auth.user?.email }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ auth.user?.status }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>
