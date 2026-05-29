<script setup lang="ts">
import { onMounted } from 'vue'
import ActivityHeatmap from '@/components/ActivityHeatmap.vue'
import { useStatisticsStore } from '@/stores/statistics'

const store = useStatisticsStore()
onMounted(async () => {
  await Promise.all([store.fetchOverview(), store.fetchActivity()])
})
</script>

<template>
  <div class="page">
    <div>
      <h1 class="page-title">活跃统计</h1>
      <p class="page-subtitle">当天提交过即记为活跃。</p>
    </div>
    <div class="metric-grid">
      <div class="metric-card"><div class="metric-label">连续活跃</div><div class="metric-value">{{ store.overview.activeDays }} 天</div></div>
      <div class="metric-card"><div class="metric-label">今日提交</div><div class="metric-value">{{ store.overview.todaySubmissions }}</div></div>
      <div class="metric-card"><div class="metric-label">已通过</div><div class="metric-value">{{ store.overview.acceptedProblems }}</div></div>
      <div class="metric-card"><div class="metric-label">正确率</div><div class="metric-value">{{ store.overview.accuracy }}%</div></div>
    </div>
    <el-card class="section-card">
      <h2 class="card-title">活跃日历</h2>
      <ActivityHeatmap :data="store.activity" :months="3" />
    </el-card>
  </div>
</template>
