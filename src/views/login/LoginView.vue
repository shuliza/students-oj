<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { DataLine, Key, UserFilled } from '@element-plus/icons-vue'
import ThemeToggle from '@/components/ui/ThemeToggle.vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const formRef = ref<FormInstance>()
const form = reactive({
  username: '',
  password: '',
  role: 'STUDENT' as 'STUDENT' | 'TEACHER'
})
const loading = ref(false)

const rules: FormRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const demoAccounts = [
  { label: '学生演示', username: 'student01', password: '123456', role: 'STUDENT' as const },
  { label: '教师演示', username: 'teacher01', password: '123456', role: 'TEACHER' as const }
]

const fillDemo = (account: (typeof demoAccounts)[number]) => {
  form.username = account.username
  form.password = account.password
  form.role = account.role
}

const login = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await auth.login(form.username, form.password, form.role)
      ElMessage.success('登录成功')
      router.push(auth.isTeacher ? '/teacher/dashboard' : '/student/dashboard')
    } finally {
      loading.value = false
    }
  })
}

const highlights = [
  '在线编写与自动判题',
  'AI 智能纠错建议',
  '学习活跃与成绩统计'
]
</script>

<!-- PLACEHOLDER_TEMPLATE -->
<template>
  <div class="login-page">
    <div class="login-topbar">
      <ThemeToggle />
    </div>

    <section class="login-visual">
      <div class="visual-inner">
        <div class="visual-brand">
          <span class="visual-mark">SQL</span>
          <span>智能 SQL OJ 判题系统</span>
        </div>
        <h1>用真实数据练 SQL，<br />即写即判，即学即会。</h1>
        <p>面向教学场景的在线判题平台，学生在线练习、自动判题，教师统一管理题库与学情。</p>
        <ul class="visual-list">
          <li v-for="item in highlights" :key="item">
            <span class="tick">✓</span>{{ item }}
          </li>
        </ul>
      </div>
    </section>

    <section class="login-panel">
      <div class="login-card">
        <h2>欢迎回来</h2>
        <p class="login-hint">请登录你的账号以继续</p>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="login">
          <el-form-item label="账号" prop="username">
            <el-input v-model="form.username" :prefix-icon="UserFilled" placeholder="请输入账号" autocomplete="username" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              :prefix-icon="Key"
              show-password
              placeholder="请输入密码"
              autocomplete="current-password"
              @keyup.enter="login"
            />
          </el-form-item>
          <el-form-item label="身份">
            <el-segmented
              v-model="form.role"
              :options="[{ label: '学生', value: 'STUDENT' }, { label: '教师', value: 'TEACHER' }]"
              block
            />
          </el-form-item>
          <el-button type="primary" size="large" class="login-button" :icon="DataLine" :loading="loading" @click="login">
            进入系统
          </el-button>
        </el-form>

        <div class="demo-row">
          <span class="demo-label">快速体验：</span>
          <el-button
            v-for="account in demoAccounts"
            :key="account.username"
            size="small"
            text
            bg
            @click="fillDemo(account)"
          >
            {{ account.label }}
          </el-button>
        </div>
      </div>
    </section>
  </div>
</template>
<!-- PLACEHOLDER_TEMPLATE -->

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  background: var(--color-bg);
}

.login-topbar {
  position: absolute;
  top: var(--space-5);
  right: var(--space-5);
  z-index: 2;
}

.login-visual {
  position: relative;
  display: flex;
  align-items: center;
  padding: var(--space-12) var(--space-10);
  color: #f8fafc;
  background:
    radial-gradient(circle at 18% 22%, rgba(99, 102, 241, 0.55), transparent 42%),
    radial-gradient(circle at 82% 78%, rgba(67, 56, 202, 0.5), transparent 46%),
    linear-gradient(135deg, #312e81 0%, #1e1b4b 55%, #0f1024 100%);
  overflow: hidden;
}

.visual-inner {
  max-width: 520px;
}

.visual-brand {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-8);
  font-size: var(--text-md);
  font-weight: 600;
  color: #e0e7ff;
}

.visual-mark {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.16);
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.02em;
}

.login-visual h1 {
  margin: 0;
  color: #ffffff;
  font-size: 40px;
  line-height: 1.18;
  font-weight: 780;
  letter-spacing: -0.01em;
}

.login-visual p {
  margin-top: var(--space-5);
  color: #c7d2fe;
  font-size: var(--text-md);
  line-height: 1.75;
}

.visual-list {
  margin: var(--space-8) 0 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.visual-list li {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  color: #e0e7ff;
  font-size: var(--text-md);
}

.tick {
  display: grid;
  place-items: center;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.18);
  color: #ffffff;
  font-size: 12px;
}

.login-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-8);
}

.login-card {
  width: 100%;
  max-width: 400px;
}

.login-card h2 {
  margin: 0;
  font-size: var(--text-2xl);
  font-weight: 760;
}

.login-hint {
  margin: var(--space-2) 0 var(--space-6);
  color: var(--color-muted);
  font-size: var(--text-base);
}

.login-button {
  width: 100%;
  margin-top: var(--space-2);
}

.demo-row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-top: var(--space-5);
  padding-top: var(--space-4);
  border-top: 1px dashed var(--color-border);
}

.demo-label {
  color: var(--color-muted);
  font-size: var(--text-sm);
}

@media (max-width: 860px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .login-visual {
    display: none;
  }

  .login-panel {
    padding: var(--space-6) var(--space-4);
  }
}
</style>
