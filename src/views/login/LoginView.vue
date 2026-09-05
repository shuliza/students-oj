<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { DataLine, Hide, Key, UserFilled, View } from '@element-plus/icons-vue'
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
const showPassword = ref(false)
const focusedField = ref<'username' | 'password' | null>(null)
const mouse = reactive({ x: 0, y: 0 })

const rules: FormRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const demoAccounts = [
  { label: '学生演示', username: 'student01', password: '123456', role: 'STUDENT' as const },
  { label: '教师演示', username: 'teacher01', password: '123456', role: 'TEACHER' as const }
]

const highlights = [
  { value: 'SQL', label: '真实数据集训练' },
  { value: 'AI', label: '智能纠错建议' },
  { value: 'OJ', label: '自动判题反馈' }
]

const characterMode = computed(() => {
  if (focusedField.value === 'password' && form.password && showPassword.value) return 'peek'
  if (focusedField.value === 'password') return 'guard'
  if (focusedField.value === 'username') return 'curious'
  return 'idle'
})

const visualStyle = computed(() => ({
  '--look-x': `${Math.max(-1, Math.min(1, (mouse.x - window.innerWidth / 2) / (window.innerWidth / 2 || 1)))}`,
  '--look-y': `${Math.max(-1, Math.min(1, (mouse.y - window.innerHeight / 2) / (window.innerHeight / 2 || 1)))}`
}))

const handleMouseMove = (event: MouseEvent) => {
  mouse.x = event.clientX
  mouse.y = event.clientY
}

onMounted(() => {
  window.addEventListener('mousemove', handleMouseMove)
})

onBeforeUnmount(() => {
  window.removeEventListener('mousemove', handleMouseMove)
})

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
</script>

<template>
  <div class="login-page" :class="`mode-${characterMode}`" :style="visualStyle">
    <div class="login-topbar">
      <ThemeToggle />
    </div>

    <section class="login-visual" aria-label="智能 SQL OJ 登录展示">
      <div class="visual-copy">
        <div class="visual-brand">
          <span class="visual-mark">SQL</span>
          <span>智能 SQL OJ 判题系统</span>
        </div>
        <h1>让每一次登录，都像进入一间会回应你的 SQL 训练室。</h1>
        <p>学生练习、自动判题、AI 纠错和教师管理集中在同一个平台里，打开就能开始推进学习进度。</p>
      </div>

      <div class="character-stage" aria-hidden="true">
        <div class="character purple">
          <div class="eye-row">
            <span class="eye"><span class="pupil" /></span>
            <span class="eye"><span class="pupil" /></span>
          </div>
        </div>
        <div class="character charcoal">
          <div class="eye-row">
            <span class="eye"><span class="pupil" /></span>
            <span class="eye"><span class="pupil" /></span>
          </div>
        </div>
        <div class="character orange">
          <div class="dot-row">
            <span class="dot" />
            <span class="dot" />
          </div>
        </div>
        <div class="character yellow">
          <div class="dot-row">
            <span class="dot" />
            <span class="dot" />
          </div>
          <span class="mouth" />
        </div>
      </div>

      <div class="highlight-strip">
        <div v-for="item in highlights" :key="item.label" class="highlight-item">
          <strong>{{ item.value }}</strong>
          <span>{{ item.label }}</span>
        </div>
      </div>
    </section>

    <section class="login-panel">
      <div class="login-card">
        <div class="mobile-brand">
          <span class="visual-mark">SQL</span>
          <span>智能 SQL OJ 判题系统</span>
        </div>

        <div class="login-heading">
          <span class="eyebrow">Welcome back</span>
          <h2>欢迎回来</h2>
          <p class="login-hint">请登录你的账号以继续</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="login">
          <el-form-item label="账号" prop="username">
            <el-input
              v-model="form.username"
              :prefix-icon="UserFilled"
              placeholder="请输入账号"
              autocomplete="username"
              @focus="focusedField = 'username'"
              @blur="focusedField = null"
              @keyup.enter="login"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              :prefix-icon="Key"
              placeholder="请输入密码"
              autocomplete="current-password"
              @focus="focusedField = 'password'"
              @blur="focusedField = null"
              @keyup.enter="login"
            >
              <template #suffix>
                <button class="password-toggle" type="button" :aria-label="showPassword ? '隐藏密码' : '显示密码'" @click="showPassword = !showPassword">
                  <el-icon><component :is="showPassword ? Hide : View" /></el-icon>
                </button>
              </template>
            </el-input>
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
          <span class="demo-label">快速体验</span>
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

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(460px, 1.08fr) minmax(420px, 0.92fr);
  background:
    radial-gradient(circle at 86% 14%, rgba(37, 99, 235, 0.12), transparent 28%),
    linear-gradient(135deg, #fff7ed 0%, #f8fafc 46%, #eef6ff 100%);
  overflow: hidden;
}

.login-topbar {
  position: absolute;
  top: 24px;
  right: 24px;
  z-index: 4;
}

.login-visual {
  position: relative;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 48px 54px 42px;
  color: #0f172a;
  overflow: hidden;
}

.login-visual::before {
  content: "";
  position: absolute;
  inset: 24px;
  border: 2px solid rgba(15, 23, 42, 0.08);
  border-radius: 28px;
  pointer-events: none;
}

.visual-copy,
.character-stage,
.highlight-strip {
  position: relative;
  z-index: 1;
}

.visual-copy {
  max-width: 590px;
}

.visual-brand,
.mobile-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #9a3412;
  font-size: 15px;
  font-weight: 760;
}

.visual-mark {
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  border: 2px solid #fed7aa;
  border-radius: 12px;
  background: #ffffff;
  color: #f97316;
  font-size: 13px;
  font-weight: 850;
  box-shadow: 0 8px 0 rgba(251, 146, 60, 0.22);
}

.login-visual h1 {
  max-width: 620px;
  margin: 56px 0 0;
  color: #172033;
  font-size: 42px;
  line-height: 1.16;
  font-weight: 820;
  letter-spacing: 0;
}

.login-visual p {
  max-width: 560px;
  margin: 20px 0 0;
  color: #475569;
  font-size: 16px;
  line-height: 1.8;
}

.character-stage {
  align-self: center;
  width: min(560px, 100%);
  height: 390px;
  margin-top: 18px;
}

.character {
  position: absolute;
  bottom: 0;
  transform-origin: bottom center;
  transition: transform 0.35s ease, height 0.35s ease, left 0.35s ease;
  box-shadow: inset -14px 0 rgba(15, 23, 42, 0.08), 0 20px 0 rgba(15, 23, 42, 0.08);
}

.purple {
  left: 82px;
  z-index: 1;
  width: 178px;
  height: 365px;
  border-radius: 16px 16px 0 0;
  background: #6c3ff5;
  transform: skewX(calc(var(--look-x) * -4deg));
}

.charcoal {
  left: 250px;
  z-index: 2;
  width: 122px;
  height: 286px;
  border-radius: 14px 14px 0 0;
  background: #2d2d2d;
  transform: skewX(calc(var(--look-x) * -5deg));
}

.orange {
  left: 0;
  z-index: 3;
  width: 236px;
  height: 190px;
  border-radius: 118px 118px 0 0;
  background: #ff9b6b;
  transform: skewX(calc(var(--look-x) * -3deg));
}

.yellow {
  left: 318px;
  z-index: 4;
  width: 142px;
  height: 224px;
  border-radius: 72px 72px 0 0;
  background: #e8d754;
  transform: skewX(calc(var(--look-x) * -3deg));
}

.eye-row,
.dot-row {
  position: absolute;
  display: flex;
  gap: 24px;
  transition: left 0.28s ease, top 0.28s ease, transform 0.28s ease;
}

.purple .eye-row {
  left: 46px;
  top: 44px;
}

.charcoal .eye-row {
  left: 28px;
  top: 34px;
  gap: 20px;
}

.orange .dot-row {
  left: 82px;
  top: 86px;
}

.yellow .dot-row {
  left: 52px;
  top: 42px;
  gap: 20px;
}

.eye {
  width: 19px;
  height: 19px;
  display: grid;
  place-items: center;
  border-radius: 999px;
  background: #ffffff;
  overflow: hidden;
}

.charcoal .eye {
  width: 17px;
  height: 17px;
}

.pupil,
.dot {
  width: 7px;
  height: 7px;
  border-radius: 999px;
  background: #2d2d2d;
  transform: translate(calc(var(--look-x) * 5px), calc(var(--look-y) * 5px));
  transition: transform 0.1s ease-out;
}

.dot {
  width: 12px;
  height: 12px;
}

.mouth {
  position: absolute;
  left: 40px;
  top: 88px;
  width: 62px;
  height: 4px;
  border-radius: 999px;
  background: #2d2d2d;
}

.mode-curious .purple {
  height: 386px;
}

.mode-guard .purple {
  height: 386px;
}

.mode-guard .charcoal {
  transform: translateX(14px) skewX(8deg);
}

.mode-peek .purple .eye-row {
  left: 22px;
  top: 38px;
}

.mode-peek .charcoal .eye-row {
  left: 12px;
  top: 30px;
}

.mode-peek .orange .dot-row {
  left: 54px;
  top: 86px;
}

.mode-peek .yellow .dot-row {
  left: 24px;
  top: 38px;
}

.highlight-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  max-width: 620px;
}

.highlight-item {
  min-height: 78px;
  padding: 15px 16px;
  border: 2px solid rgba(254, 215, 170, 0.9);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0 10px 0 rgba(251, 146, 60, 0.12);
}

.highlight-item strong {
  display: block;
  color: #2563eb;
  font-size: 19px;
  line-height: 1;
  font-weight: 850;
}

.highlight-item span {
  display: block;
  margin-top: 9px;
  color: #9a3412;
  font-size: 13px;
  font-weight: 700;
}

.login-panel {
  position: relative;
  z-index: 2;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 72px 42px;
  background: rgba(255, 255, 255, 0.72);
  box-shadow: -24px 0 70px rgba(15, 23, 42, 0.08);
}

.login-card {
  width: 100%;
  max-width: 430px;
  padding: 34px;
  border: 2px solid #fed7aa;
  border-radius: 22px;
  background: #ffffff;
  box-shadow: 0 14px 0 rgba(251, 146, 60, 0.18), 0 28px 70px rgba(15, 23, 42, 0.12);
}

.mobile-brand {
  display: none;
  margin-bottom: 30px;
}

.login-heading {
  margin-bottom: 28px;
}

.eyebrow {
  color: #2563eb;
  font-size: 12px;
  font-weight: 800;
  text-transform: uppercase;
}

.login-card h2 {
  margin: 8px 0 0;
  color: #172033;
  font-size: 30px;
  line-height: 1.15;
  font-weight: 820;
}

.login-hint {
  margin: 10px 0 0;
  color: #64748b;
  font-size: 15px;
}

.password-toggle {
  width: 30px;
  height: 30px;
  display: grid;
  place-items: center;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: #64748b;
  cursor: pointer;
  transition: background-color 0.18s ease, color 0.18s ease;
}

.password-toggle:hover {
  background: #fff7ed;
  color: #f97316;
}

.login-button {
  width: 100%;
  min-height: 46px;
  margin-top: 4px;
}

.demo-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 22px;
  padding-top: 18px;
  border-top: 1px dashed #fed7aa;
  flex-wrap: wrap;
}

.demo-label {
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
}

:deep(.el-form-item__label) {
  color: #334155;
  font-weight: 750;
}

:deep(.el-input__wrapper) {
  min-height: 46px;
  border-radius: 12px;
  box-shadow: 0 0 0 1px #fed7aa inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #f97316 inset, 0 0 0 4px rgba(249, 115, 22, 0.12);
}

:deep(.el-segmented) {
  --el-segmented-item-selected-bg-color: #f97316;
  --el-segmented-item-selected-color: #0f172a;
  --el-segmented-bg-color: #fff7ed;
  border: 1px solid #fed7aa;
  border-radius: 12px;
}

:deep(.el-button--primary) {
  --el-button-bg-color: #f97316;
  --el-button-border-color: #f97316;
  --el-button-hover-bg-color: #fb923c;
  --el-button-hover-border-color: #fb923c;
  --el-button-text-color: #0f172a;
  font-weight: 800;
}

@media (prefers-reduced-motion: reduce) {
  .character,
  .eye-row,
  .dot-row,
  .pupil,
  .dot,
  .password-toggle {
    transition: none;
  }
}

@media (max-width: 980px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .login-visual {
    min-height: auto;
    padding: 32px 24px 0;
  }

  .login-visual::before,
  .visual-copy,
  .highlight-strip {
    display: none;
  }

  .character-stage {
    height: 210px;
    transform: scale(0.58);
    transform-origin: bottom center;
    margin: 0 auto -24px;
  }

  .login-panel {
    min-height: calc(100vh - 186px);
    padding: 24px;
    background: transparent;
    box-shadow: none;
  }

  .mobile-brand {
    display: flex;
  }
}

@media (max-width: 560px) {
  .login-topbar {
    top: 16px;
    right: 16px;
  }

  .login-visual {
    padding-top: 54px;
  }

  .login-card {
    padding: 24px;
    border-radius: 18px;
  }

  .login-card h2 {
    font-size: 26px;
  }

  .demo-row {
    align-items: stretch;
  }
}
</style>
