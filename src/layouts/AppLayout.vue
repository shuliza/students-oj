<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  ArrowDown,
  Collection,
  DataAnalysis,
  Download,
  EditPen,
  Expand,
  Fold,
  List,
  Menu as MenuIcon,
  School,
  SwitchButton,
  TrendCharts,
  User,
  UserFilled
} from '@element-plus/icons-vue'
import ThemeToggle from '@/components/ui/ThemeToggle.vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const collapsed = ref(false)
const mobileOpen = ref(false)

const isTeacherArea = computed(() => route.path.startsWith('/teacher'))
const isSqlWorkspace = computed(() => route.name === 'sql-editor')
const brandTitle = computed(() => 'SQL OJ')
const brandSub = computed(() => (isTeacherArea.value ? '教师工作台' : '学习中心'))

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
const currentTitle = computed(() => {
  const hit = menus.value.find(
    (m) => route.path === m.path || route.path.startsWith(m.path.replace(/\/\d+$/, '') + '/')
  )
  return (route.meta.title as string) ?? hit?.label ?? 'SQL OJ'
})

const isActive = (path: string) => {
  const base = path.replace(/\/\d+$/, '')
  return route.path === path || route.path.startsWith(base + '/') || route.path.startsWith(path + '/')
}

const navigate = (path: string) => {
  mobileOpen.value = false
  router.push(path)
}

const logout = async () => {
  try {
    await ElMessageBox.confirm('确认退出登录？', '提示', { type: 'warning' })
    await auth.logout()
    router.push('/login')
  } catch {}
}

const initials = computed(() => auth.user?.realName?.slice(0, 1) ?? 'U')
</script>

<!-- PLACEHOLDER_TEMPLATE -->
<template>
  <!-- SQL 工作台：全屏沉浸，不套用通用布局 -->
  <div v-if="isSqlWorkspace" class="workspace-shell">
    <router-view />
  </div>

  <div v-else class="layout" :class="{ collapsed }">
    <!-- 移动端遮罩 -->
    <div v-if="mobileOpen" class="scrim" @click="mobileOpen = false"></div>

    <aside class="sidebar" :class="{ open: mobileOpen }">
      <div class="brand">
        <span class="brand-mark">SQL</span>
        <div v-show="!collapsed" class="brand-text">
          <strong>{{ brandTitle }}</strong>
          <span>{{ brandSub }}</span>
        </div>
      </div>

      <nav class="menu">
        <a
          v-for="item in menus"
          :key="item.path"
          class="menu-item"
          :class="{ active: isActive(item.path) }"
          :title="collapsed ? item.label : ''"
          @click="navigate(item.path)"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span v-show="!collapsed">{{ item.label }}</span>
        </a>
      </nav>

      <button class="collapse-btn" type="button" @click="collapsed = !collapsed">
        <el-icon><component :is="collapsed ? Expand : Fold" /></el-icon>
        <span v-show="!collapsed">收起菜单</span>
      </button>
    </aside>

    <div class="main">
      <header class="header">
        <div class="header-left">
          <button class="icon-btn mobile-only" type="button" @click="mobileOpen = true">
            <el-icon><MenuIcon /></el-icon>
          </button>
          <div class="crumb">
            <span class="crumb-root">{{ brandSub }}</span>
            <span class="crumb-sep">/</span>
            <strong class="crumb-current">{{ currentTitle }}</strong>
          </div>
        </div>

        <div class="header-right">
          <ThemeToggle />
          <el-dropdown trigger="click">
            <div class="user-chip">
              <span class="avatar">{{ initials }}</span>
              <div class="user-meta">
                <strong>{{ auth.user?.realName ?? '用户' }}</strong>
                <span>{{ auth.user?.role ?? 'GUEST' }}</span>
              </div>
              <el-icon class="chev"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :icon="SwitchButton" @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <section class="content">
        <router-view />
      </section>
    </div>
  </div>
</template>
<!-- PLACEHOLDER_TEMPLATE -->

<style scoped>
.workspace-shell {
  min-height: 100vh;
}

.layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 248px minmax(0, 1fr);
  background: var(--color-bg);
  transition: grid-template-columns 0.2s ease;
}

.layout.collapsed {
  grid-template-columns: 76px minmax(0, 1fr);
}

.sidebar {
  position: sticky;
  top: 0;
  height: 100vh;
  display: flex;
  flex-direction: column;
  padding: var(--space-5) var(--space-3);
  background: var(--color-sidebar);
  border-right: 1px solid var(--color-border);
}

.brand {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: 0 var(--space-2) var(--space-6);
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  flex: 0 0 38px;
  border-radius: var(--radius-md);
  background: var(--color-primary);
  color: var(--color-on-primary);
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.02em;
}

.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.brand-text strong {
  color: var(--color-text-strong);
  font-size: var(--text-md);
  font-weight: 750;
}

.brand-text span {
  color: var(--color-muted);
  font-size: var(--text-xs);
}

.menu {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  flex: 1;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  min-height: 42px;
  padding: 0 var(--space-3);
  border-radius: var(--radius-md);
  color: var(--color-sidebar-text);
  font-size: var(--text-base);
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
}

.menu-item .el-icon {
  flex: 0 0 18px;
  font-size: 18px;
}

.menu-item:hover {
  background: var(--color-surface-3);
  color: var(--color-text-strong);
}

.menu-item.active {
  background: var(--color-sidebar-active-bg);
  color: var(--color-sidebar-active-text);
}

.collapse-btn {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  min-height: 40px;
  padding: 0 var(--space-3);
  margin-top: var(--space-3);
  border: 0;
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--color-muted);
  font-size: var(--text-sm);
  font-weight: 600;
  cursor: pointer;
}

.collapse-btn:hover {
  background: var(--color-surface-3);
  color: var(--color-text);
}

.main {
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.header {
  position: sticky;
  top: 0;
  z-index: var(--z-sticky);
  height: 64px;
  padding: 0 var(--space-6);
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--color-border);
  background: color-mix(in srgb, var(--color-surface) 85%, transparent);
  backdrop-filter: blur(12px);
}

.header-left,
.header-right {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.crumb {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-base);
}

.crumb-root {
  color: var(--color-muted);
}

.crumb-sep {
  color: var(--color-subtle);
}

.crumb-current {
  color: var(--color-text-strong);
  font-weight: 700;
}

.icon-btn {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  color: var(--color-muted);
  cursor: pointer;
  font-size: 18px;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-md);
  cursor: pointer;
}

.user-chip:hover {
  background: var(--color-surface-3);
}

.avatar {
  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--color-on-primary);
  font-size: var(--text-sm);
  font-weight: 700;
}

.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.user-meta strong {
  font-size: var(--text-sm);
  color: var(--color-text-strong);
}

.user-meta span {
  font-size: var(--text-xs);
  color: var(--color-muted);
}

.chev {
  color: var(--color-subtle);
  font-size: 12px;
}

.content {
  flex: 1;
  padding: var(--space-6);
}

.scrim {
  display: none;
}

.mobile-only {
  display: none;
}

@media (max-width: 920px) {
  .layout,
  .layout.collapsed {
    grid-template-columns: 1fr;
  }

  .sidebar {
    position: fixed;
    top: 0;
    left: 0;
    z-index: var(--z-drawer);
    width: 248px;
    transform: translateX(-100%);
    transition: transform 0.22s ease;
  }

  .sidebar.open {
    transform: translateX(0);
    box-shadow: var(--shadow-pop);
  }

  .scrim {
    display: block;
    position: fixed;
    inset: 0;
    z-index: calc(var(--z-drawer) - 1);
    background: rgba(15, 23, 42, 0.45);
  }

  .mobile-only {
    display: grid;
  }

  .user-meta {
    display: none;
  }

  .content {
    padding: var(--space-4);
  }
}
</style>
