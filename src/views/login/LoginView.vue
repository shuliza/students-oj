<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const username = ref('student01')
const password = ref('123456')
const role = ref<'STUDENT' | 'TEACHER'>('STUDENT')
const loading = ref(false)

const login = async () => {
  loading.value = true
  try {
    await auth.login(username.value, password.value, role.value)
    ElMessage.success('登录成功')
    router.push(auth.isTeacher ? '/teacher/dashboard' : '/student/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <section class="login-visual">
      <h1>智能 SQL OJ 判题系统</h1>
      <p>在线练习、自动判题、学习统计与教师题库管理。</p>
    </section>
    <section class="login-panel">
      <el-card class="login-card">
        <h2>登录</h2>
        <el-form label-position="top">
          <el-form-item label="账号">
            <el-input v-model="username" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="password" type="password" show-password />
          </el-form-item>
          <el-form-item label="身份">
            <el-segmented v-model="role" :options="[{ label: '学生', value: 'STUDENT' }, { label: '教师', value: 'TEACHER' }]" />
          </el-form-item>
          <el-button type="primary" size="large" class="login-button" :loading="loading" @click="login">进入系统</el-button>
        </el-form>
      </el-card>
    </section>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  background: var(--color-bg);
}

.login-visual {
  padding: 80px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: var(--color-sidebar);
  color: #ffffff;
}

.login-visual h1 {
  margin: 0;
  font-size: 48px;
}

.login-visual p {
  margin-top: 18px;
  color: #cbd5e1;
  font-size: 18px;
}

.login-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-card {
  width: 420px;
  border-radius: 8px;
}

.login-card h2 {
  margin: 0 0 24px;
}

.login-button {
  width: 100%;
}
</style>
