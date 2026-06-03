<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DataLine, Key, UserFilled } from '@element-plus/icons-vue'
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
            <el-input v-model="username" :prefix-icon="UserFilled" autocomplete="username" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="password" type="password" :prefix-icon="Key" show-password autocomplete="current-password" />
          </el-form-item>
          <el-form-item label="身份">
            <el-segmented v-model="role" :options="[{ label: '学生', value: 'STUDENT' }, { label: '教师', value: 'TEACHER' }]" />
          </el-form-item>
          <el-button type="primary" size="large" class="login-button" :icon="DataLine" :loading="loading" @click="login">进入系统</el-button>
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
  background:
    radial-gradient(circle at 12% 18%, rgba(147, 197, 253, 0.38), transparent 340px),
    linear-gradient(135deg, #eef5ff 0%, #f8fafc 48%, #ffffff 100%);
}

.login-visual {
  padding: 80px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background:
    linear-gradient(135deg, rgba(30, 64, 175, 0.34), rgba(15, 23, 42, 0.78)),
    var(--color-sidebar);
  color: #ffffff;
}

.login-visual h1 {
  margin: 0;
  max-width: 640px;
  font-size: 48px;
  line-height: 1.12;
  font-weight: 780;
}

.login-visual p {
  margin-top: 18px;
  max-width: 560px;
  color: #dbeafe;
  font-size: 18px;
  line-height: 1.75;
}

.login-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-card {
  width: 420px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-raised);
}

.login-card h2 {
  margin: 0 0 24px;
  color: #0f172a;
  font-size: 24px;
  font-weight: 760;
}

.login-button {
  width: 100%;
  margin-top: 8px;
}

@media (max-width: 860px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .login-visual {
    padding: 48px 28px;
  }

  .login-visual h1 {
    font-size: 34px;
  }

  .login-panel {
    padding: 28px 18px 40px;
  }

  .login-card {
    width: 100%;
    max-width: 440px;
  }
}
</style>
