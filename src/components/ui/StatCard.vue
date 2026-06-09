<script setup lang="ts">
import { computed } from 'vue'
import type { Component } from 'vue'
import { CaretBottom, CaretTop } from '@element-plus/icons-vue'
import ChartBox from './ChartBox.vue'

const props = withDefaults(defineProps<{
  label: string
  value: string | number
  icon?: Component
  /** 环比变化百分比，正负决定箭头与颜色 */
  trend?: number
  /** 迷你趋势数据 */
  spark?: number[]
  accent?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
}>(), {
  accent: 'primary'
})

const sparkOption = computed(() => ({
  grid: { left: 0, right: 0, top: 4, bottom: 0 },
  xAxis: { type: 'category', show: false, data: (props.spark ?? []).map((_, i) => i) },
  yAxis: { type: 'value', show: false },
  tooltip: { show: false },
  series: [
    {
      type: 'line',
      data: props.spark ?? [],
      smooth: true,
      symbol: 'none',
      lineStyle: { width: 2, color: 'var(--color-primary)' },
      areaStyle: { opacity: 0.12, color: 'var(--color-primary)' }
    }
  ]
}))
</script>

<template>
  <div class="stat-card" :class="`accent-${accent}`">
    <div class="stat-head">
      <span class="stat-label">{{ label }}</span>
      <span v-if="icon" class="stat-icon"><el-icon><component :is="icon" /></el-icon></span>
    </div>
    <div class="stat-value">{{ value }}</div>
    <div class="stat-foot">
      <span
        v-if="typeof trend === 'number'"
        class="stat-trend"
        :class="trend >= 0 ? 'up' : 'down'"
      >
        <el-icon><component :is="trend >= 0 ? CaretTop : CaretBottom" /></el-icon>
        {{ Math.abs(trend) }}%
      </span>
      <div v-if="spark && spark.length" class="stat-spark">
        <ChartBox :option="sparkOption" height="34px" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.stat-card {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  min-height: 120px;
  padding: var(--space-5);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-raised);
}

.stat-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-2);
}

.stat-label {
  color: var(--color-muted);
  font-size: var(--text-sm);
  font-weight: 600;
}

.stat-icon {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border-radius: var(--radius-md);
  background: var(--color-primary-soft);
  color: var(--color-primary);
  font-size: 18px;
}

.accent-success .stat-icon { background: var(--color-success-soft); color: var(--color-success); }
.accent-warning .stat-icon { background: var(--color-warning-soft); color: var(--color-warning); }
.accent-danger .stat-icon { background: var(--color-danger-soft); color: var(--color-danger); }
.accent-info .stat-icon { background: var(--color-info-soft); color: var(--color-info); }

.stat-value {
  color: var(--color-text-strong);
  font-size: var(--text-3xl);
  line-height: 1;
  font-weight: 760;
}

.stat-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  margin-top: auto;
}

.stat-trend {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-size: var(--text-xs);
  font-weight: 650;
}

.stat-trend.up { color: var(--color-success); }
.stat-trend.down { color: var(--color-danger); }

.stat-spark {
  flex: 1;
  max-width: 120px;
}
</style>
