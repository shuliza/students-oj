<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  status: string
}>()

type Tone = 'success' | 'danger' | 'warning' | 'info' | 'pending'

const map: Record<string, { label: string; tone: Tone }> = {
  ACCEPTED: { label: '通过', tone: 'success' },
  WRONG_ANSWER: { label: 'WRONG ANSWER', tone: 'danger' },
  WA: { label: 'WA', tone: 'danger' },
  TIME_LIMIT: { label: 'TIME LIMIT', tone: 'warning' },
  TIME_LIMIT_EXCEEDED: { label: 'TIME LIMIT', tone: 'warning' },
  RESULT_LIMIT_EXCEEDED: { label: '结果过大', tone: 'warning' },
  SYSTEM_BUSY: { label: '系统繁忙', tone: 'warning' },
  TLE: { label: 'TLE', tone: 'warning' },
  RUNTIME_ERROR: { label: 'RUNTIME ERROR', tone: 'info' },
  RE: { label: 'RE', tone: 'info' },
  PENDING: { label: 'PENDING', tone: 'pending' },
  TODO: { label: '未做过', tone: 'info' },
  FAILED: { label: '未通过', tone: 'danger' }
}

const entry = computed(() => map[props.status] ?? { label: props.status, tone: 'info' as Tone })
</script>

<template>
  <span class="status-tag" :class="`tone-${entry.tone}`">
    <span class="dot"></span>
    {{ entry.label }}
  </span>
</template>

<style scoped>
.status-tag {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  height: 24px;
  padding: 0 var(--space-3);
  border-radius: var(--radius-pill);
  font-size: var(--text-xs);
  font-weight: 650;
  white-space: nowrap;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.tone-success {
  color: var(--color-success);
  background: var(--color-success-soft);
}

.tone-danger {
  color: var(--color-danger);
  background: var(--color-danger-soft);
}

.tone-warning {
  color: var(--color-warning);
  background: var(--color-warning-soft);
}

.tone-info {
  color: var(--color-info);
  background: var(--color-info-soft);
}

.tone-pending {
  color: var(--color-primary);
  background: var(--color-primary-soft);
}
</style>
