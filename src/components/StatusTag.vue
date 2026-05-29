<script setup lang="ts">
defineProps<{
  status: string
}>()

const statusMap: Record<string, { label: string; type: 'success' | 'warning' | 'danger' | 'info' | 'primary' }> = {
  ACCEPTED: { label: '通过', type: 'success' },
  WRONG_ANSWER: { label: 'WRONG ANSWER', type: 'danger' },
  TIME_LIMIT: { label: 'TIME LIMIT', type: 'warning' },
  RUNTIME_ERROR: { label: 'RUNTIME ERROR', type: 'info' },
  WA: { label: 'WA', type: 'danger' },
  TLE: { label: 'TLE', type: 'warning' },
  RE: { label: 'RE', type: 'info' },
  PENDING: { label: 'PENDING', type: 'primary' },
  TODO: { label: '未做过', type: 'info' },
  FAILED: { label: '未通过', type: 'danger' }
}
</script>

<template>
  <el-tag :type="statusMap[status]?.type ?? 'info'" round>
    {{ statusMap[status]?.label ?? status }}
  </el-tag>
</template>
