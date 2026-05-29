<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const isTeacherArea = computed(() => route.path.startsWith('/teacher'))
const isSqlWorkspace = computed(() => route.name === 'sql-editor')
const brand = computed(() => (isTeacherArea.value ? 'SQL OJ 教师端' : 'SQL OJ 学生端'))

const studentMenus = [
  { label: '学习首页', path: '/student/dashboard' },
  { label: '题目列表', path: '/student/problems' },
  { label: 'SQL 练习', path: '/student/editor/101' },
  { label: '活跃统计', path: '/student/activity' },
  { label: '个人中心', path: '/student/profile' }
]

const teacherMenus = [
  { label: '数据看板', path: '/teacher/dashboard' },
  { label: '学生管理', path: '/teacher/students' },
  { label: '学生分组', path: '/teacher/groups' },
  { label: '题库管理', path: '/teacher/problems' },
  { label: '成绩导出', path: '/teacher/export' }
]

const menus = computed(() => (isTeacherArea.value ? teacherMenus : studentMenus))

const logout = async () => {
  await auth.logout()
  router.push('/login')
}
</script>

<template>
  <div class="layout" :class="{ 'workspace-layout': isSqlWorkspace }">
    <aside v-if="!isSqlWorkspace" class="sidebar">
      <div class="brand">{{ brand }}</div>
      <nav class="menu">
        <router-link
          v-for="item in menus"
          :key="item.path"
          :to="item.path"
          class="menu-item"
          :class="{ active: route.path === item.path || route.path.startsWith(item.path + '/') }"
        >
          {{ item.label }}
        </router-link>
      </nav>
    </aside>
    <main class="main">
      <header v-if="!isSqlWorkspace" class="header">
        <div>
          <strong>{{ route.meta.title ?? '智能 SQL OJ 判题系统' }}</strong>
          <span class="header-sub">Vue3 / TypeScript / Element Plus</span>
        </div>
        <div class="user">
          <el-tag effect="plain">{{ auth.user?.role ?? 'GUEST' }}</el-tag>
          <span>{{ auth.user?.realName }}</span>
          <el-button size="small" @click="logout">退出</el-button>
        </div>
      </header>
      <section class="content" :class="{ 'workspace-content': isSqlWorkspace }">
        <router-view />
      </section>
    </main>
  </div>
</template>

<style scoped>
.layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
}

.workspace-layout {
  display: block;
  background: #171717;
}

.sidebar {
  position: sticky;
  top: 0;
  height: 100vh;
  padding: 28px 18px;
  background: var(--color-sidebar);
  color: #cbd5e1;
}

.brand {
  margin-bottom: 36px;
  font-size: 24px;
  font-weight: 700;
}

.menu {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.menu-item {
  padding: 14px 20px;
  border-radius: 8px;
  color: #cbd5e1;
}

.menu-item.active,
.menu-item:hover {
  background: var(--color-primary);
  color: #ffffff;
}

.main {
  min-width: 0;
}

.header {
  height: 72px;
  padding: 0 30px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e2e8f0;
  background: #ffffff;
}

.header-sub {
  margin-left: 12px;
  color: var(--color-muted);
  font-size: 13px;
}

.user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.content {
  padding: 28px 30px 44px;
}

.workspace-content {
  height: 100vh;
  padding: 0;
  overflow: hidden;
}
</style>
