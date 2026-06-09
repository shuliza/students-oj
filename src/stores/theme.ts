import { defineStore } from 'pinia'

export type ThemeMode = 'light' | 'dark'

const STORAGE_KEY = 'theme-mode'

const resolveInitial = (): ThemeMode => {
  const saved = localStorage.getItem(STORAGE_KEY)
  if (saved === 'light' || saved === 'dark') return saved
  const prefersDark = window.matchMedia?.('(prefers-color-scheme: dark)').matches
  return prefersDark ? 'dark' : 'light'
}

const applyTheme = (mode: ThemeMode) => {
  document.documentElement.setAttribute('data-theme', mode)
}

export const useThemeStore = defineStore('theme', {
  state: () => ({
    mode: 'light' as ThemeMode
  }),
  getters: {
    isDark: (state) => state.mode === 'dark'
  },
  actions: {
    init() {
      this.mode = resolveInitial()
      applyTheme(this.mode)
    },
    setMode(mode: ThemeMode) {
      this.mode = mode
      localStorage.setItem(STORAGE_KEY, mode)
      applyTheme(mode)
    },
    toggle() {
      this.setMode(this.mode === 'light' ? 'dark' : 'light')
    }
  }
})
