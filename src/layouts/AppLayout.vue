<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Collection,
  DataAnalysis,
  Download,
  EditPen,
  List,
  School,
  SwitchButton,
  TrendCharts,
  User,
  UserFilled
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const isTeacherArea = computed(() => route.path.startsWith('/teacher'))
const isSqlWorkspace = computed(() => route.name === 'sql-editor')
const brand = computed(() => (isTeacherArea.value ? 'SQL OJ 教师端' : 'SQL OJ 学生端'))

const studentMenus = [
  { label: '学习首页', path: '/student/dashboard', icon: DataAnalysis },
  { label: '题目列表', path: '/student/problems', icon: List },
  { label: 'SQL 练习', path: '/student/editor/101', icon: EditPen },
  { label: '活跃统计', path: '/student/activity', icon: TrendCharts },
  { label: '个人中心', path: '/student/profile', icon: User }
]

const teacherMenus = [
  { label: '数据看板', path: '/teacher/dashboard', icon: DataAnalysis },
  { label: '学生管理', path: '/teacher/students', icon: UserFilled },
  { label: '学生分组', path: '/teacher/groups', icon: School },
  { label: '题库管理', path: '/teacher/problems', icon: Collection },
  { label: '成绩导出', path: '/teacher/export', icon: Download }
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
      <div class="rail-label">SQL Training</div>
      <nav class="menu">
        <router-link
          v-for="item in menus"
          :key="item.path"
          :to="item.path"
          class="menu-item"
          :class="{ active: route.path === item.path || route.path.startsWith(item.path + '/') }"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
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
          <el-button size="small" :icon="SwitchButton" @click="logout">退出</el-button>
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
  grid-template-columns: 252px minmax(0, 1fr);
  background:
    radial-gradient(circle at top left, rgba(219, 234, 254, 0.86), transparent 360px),
    var(--color-bg);
}

.workspace-layout {
  display: block;
  background: #171717;
}

.sidebar {
  position: sticky;
  top: 0;
  height: 100vh;
  padding: 26px 16px;
  background:
    linear-gradient(180deg, rgba(30, 64, 175, 0.18), rgba(15, 23, 42, 0) 44%),
    var(--color-sidebar);
  color: #dbe5f4;
  box-shadow: inset -1px 0 0 rgba(255, 255, 255, 0.08);
}

.brand {
  margin-bottom: 6px;
  padding: 0 10px;
  color: #ffffff;
  font-size: 24px;
  line-height: 1.25;
  font-weight: 780;
}

.rail-label {
  margin: 0 10px 30px;
  color: #93a4bd;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.menu {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.menu-item {
  min-height: 44px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-radius: 8px;
  color: #cbd5e1;
  font-size: 14px;
  font-weight: 650;
}

.menu-item .el-icon {
  width: 18px;
  color: #8ea2be;
  font-size: 18px;
}

.menu-item.active,
.menu-item:hover {
  background: #1e40af;
  color: #ffffff;
}

.menu-item.active .el-icon,
.menu-item:hover .el-icon {
  color: #ffffff;
}

.main {
  min-width: 0;
}

.header {
  height: 68px;
  padding: 0 28px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(216, 225, 238, 0.9);
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(12px);
}

.header strong {
  color: #0f172a;
  font-size: 15px;
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
  color: #334155;
  font-size: 14px;
}

.content {
  padding: 26px 28px 42px;
}

.workspace-content {
  height: 100vh;
  padding: 0;
  overflow: hidden;
}

@media (max-width: 920px) {
  .layout {
    grid-template-columns: 1fr;
  }

  .sidebar {
    position: static;
    height: auto;
    padding: 16px;
  }

  .menu {
    flex-direction: row;
    overflow-x: auto;
  }

  .menu-item {
    flex: 0 0 auto;
  }

  .header {
    height: auto;
    min-height: 64px;
    align-items: flex-start;
    gap: 12px;
    padding: 16px;
    flex-direction: column;
  }

  .content {
    padding: 18px 16px 32px;
  }
}
</style>
