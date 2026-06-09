<script setup lang="ts">
import { Moon, Sunny } from '@element-plus/icons-vue'
import { useThemeStore } from '@/stores/theme'

const theme = useThemeStore()
</script>

<template>
  <button
    class="theme-toggle"
    type="button"
    :aria-label="theme.isDark ? '切换到亮色模式' : '切换到暗色模式'"
    :title="theme.isDark ? '切换到亮色模式' : '切换到暗色模式'"
    @click="theme.toggle()"
  >
    <el-icon>
      <Moon v-if="theme.isDark" />
      <Sunny v-else />
    </el-icon>
  </button>
</template>

<style scoped>
.theme-toggle {
  display: inline-grid;
  place-items: center;
  width: 36px;
  height: 36px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  color: var(--color-muted);
  cursor: pointer;
  font-size: 17px;
  transition: color 0.18s ease, border-color 0.18s ease, background 0.18s ease;
}

.theme-toggle:hover {
  color: var(--color-primary);
  border-color: var(--color-primary);
  background: var(--color-primary-soft);
}
</style>
