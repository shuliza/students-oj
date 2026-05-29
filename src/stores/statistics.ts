import { defineStore } from 'pinia'
import { statisticsApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import type { ActivityItem } from '@/types'

export const useStatisticsStore = defineStore('statistics', {
  state: () => ({
    overview: {
      todaySubmissions: 0,
      acceptedProblems: 0,
      activeDays: 0,
      accuracy: 0,
      students: 0,
      problems: 0,
      submissions: 0,
      passRate: 0,
      todayAttempted: 0,
      todayPassed: 0
    },
    activity: [] as ActivityItem[]
  }),
  actions: {
    async fetchOverview(params?: { groupName?: string; studentId?: number }) {
      const auth = useAuthStore()
      this.overview = auth.isTeacher ? await statisticsApi.teacherOverview(params) : await statisticsApi.overview()
    },
    async fetchActivity(params?: { groupName?: string; studentId?: number }) {
      const auth = useAuthStore()
      this.activity = auth.isTeacher ? await statisticsApi.teacherActivity(params) : await statisticsApi.activity()
    }
  }
})
