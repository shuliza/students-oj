<script setup lang="ts">
import { Box } from '@element-plus/icons-vue'

withDefaults(defineProps<{
  description?: string
  minHeight?: string
}>(), {
  description: '暂无数据',
  minHeight: '220px'
})
</script>

<template>
  <div class="empty-state" :style="{ minHeight }">
    <div class="empty-icon">
      <slot name="icon">
        <el-icon><Box /></el-icon>
      </slot>
    </div>
    <p class="empty-text">{{ description }}</p>
    <div v-if="$slots.action" class="empty-action">
      <slot name="action" />
    </div>
  </div>
</template>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  padding: var(--space-8) var(--space-4);
  text-align: center;
}

.empty-icon {
  display: grid;
  place-items: center;
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  background: var(--color-surface-3);
  color: var(--color-subtle);
  font-size: 26px;
}

.empty-text {
  margin: 0;
  color: var(--color-muted);
  font-size: var(--text-base);
}
</style>
