import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', name: 'login', component: () => import('@/views/login/LoginView.vue') },
    {
      path: '/student',
      component: () => import('@/layouts/AppLayout.vue'),
      meta: { requiresAuth: true, role: 'STUDENT' },
      children: [
        { path: 'dashboard', name: 'student-dashboard', component: () => import('@/views/student/DashboardView.vue') },
        { path: 'problems', name: 'problem-list', component: () => import('@/views/student/ProblemListView.vue') },
        { path: 'problems/:id', name: 'problem-detail', component: () => import('@/views/student/ProblemDetailView.vue') },
        { path: 'editor/:id?', name: 'sql-editor', component: () => import('@/views/student/SqlEditorView.vue') },
        { path: 'profile', name: 'profile', component: () => import('@/views/student/ProfileView.vue') },
        { path: 'activity', name: 'activity', component: () => import('@/views/student/ActivityView.vue') }
      ]
    },
    {
      path: '/teacher',
      component: () => import('@/layouts/AppLayout.vue'),
      meta: { requiresAuth: true, role: 'TEACHER' },
      children: [
        { path: 'dashboard', name: 'teacher-dashboard', component: () => import('@/views/teacher/TeacherDashboardView.vue') },
        { path: 'students', name: 'student-management', component: () => import('@/views/teacher/StudentManagementView.vue') },
        { path: 'groups', name: 'group-management', component: () => import('@/views/teacher/GroupManagementView.vue') },
        { path: 'problems', name: 'problem-management', component: () => import('@/views/teacher/ProblemManagementView.vue') },
        { path: 'export', name: 'export-center', component: () => import('@/views/teacher/ExportCenterView.vue') }
      ]
    }
  ]
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isLoggedIn) return '/login'
  if (auth.isLoggedIn) {
    await auth.refreshProfile()
  }
  if (to.path.startsWith('/teacher') && auth.isLoggedIn && !auth.isTeacher) return '/student/dashboard'
  if (to.path.startsWith('/student') && auth.isLoggedIn && auth.isTeacher) return '/teacher/dashboard'
  if (to.path === '/login' && auth.isLoggedIn) return auth.isTeacher ? '/teacher/dashboard' : '/student/dashboard'
  return true
})

export default router
