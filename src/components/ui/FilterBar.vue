<script setup lang="ts">
import { Search } from '@element-plus/icons-vue'

defineProps<{
  /** 是否显示搜索框 */
  searchable?: boolean
  searchPlaceholder?: string
}>()

const keyword = defineModel<string>('keyword', { default: '' })
</script>

<template>
  <div class="filter-bar">
    <div v-if="searchable !== false" class="filter-search">
      <el-input
        v-model="keyword"
        :placeholder="searchPlaceholder ?? '搜索'"
        :prefix-icon="Search"
        clearable
      />
    </div>
    <div class="filter-controls">
      <slot />
    </div>
    <div v-if="$slots.actions" class="filter-actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<style scoped>
.filter-bar {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-wrap: wrap;
  padding: var(--space-3) var(--space-4);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-xs);
}

.filter-search {
  width: 240px;
}

.filter-controls {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.filter-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

@media (max-width: 720px) {
  .filter-search {
    width: 100%;
  }

  .filter-actions {
    margin-left: 0;
    width: 100%;
  }
}
</style>
