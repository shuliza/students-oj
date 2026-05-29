import { defineStore } from 'pinia'
import { problemApi, submissionApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { problems as mockProblems, submissions as mockSubmissions } from '@/api/mock'
import type { Problem, Submission } from '@/types'

export const useProblemStore = defineStore('problem', {
  state: () => ({
    problems: [] as Problem[],
    submissions: [] as Submission[]
  }),
  actions: {
    async fetchProblems() {
      try {
        this.problems = await problemApi.list()
      } catch {
        this.problems = mockProblems
      }
    },
    async fetchSubmissions(params?: { groupName?: string; studentId?: number }) {
      try {
        const auth = useAuthStore()
        this.submissions = auth.isTeacher ? await submissionApi.list(params) : await submissionApi.mine()
      } catch {
        this.submissions = mockSubmissions
      }
    }
  }
})
