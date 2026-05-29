import { defineStore } from 'pinia'
import { authApi } from '@/api'
import type { User } from '@/types'

interface AuthState {
  token: string
  user: User | null
  profileLoaded: boolean
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: localStorage.getItem('token') ?? '',
    user: JSON.parse(localStorage.getItem('user') ?? 'null') as User | null,
    profileLoaded: false
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    isTeacher: (state) => state.user?.role === 'TEACHER' || state.user?.role === 'ADMIN'
  },
  actions: {
    async login(username: string, password: string, role: 'STUDENT' | 'TEACHER') {
      const result = await authApi.login(username, password, role)
      this.token = result.token
      this.user = result.user
      this.profileLoaded = true
      localStorage.setItem('token', result.token)
      localStorage.setItem('user', JSON.stringify(result.user))
    },
    async refreshProfile(force = false) {
      if (!this.token || (!force && this.profileLoaded)) {
        return
      }
      const user = await authApi.me()
      this.user = user
      this.profileLoaded = true
      localStorage.setItem('user', JSON.stringify(user))
    },
    async logout() {
      if (this.token) {
        await authApi.logout().catch(() => undefined)
      }
      this.token = ''
      this.user = null
      this.profileLoaded = false
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
