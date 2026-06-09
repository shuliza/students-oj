<script setup lang="ts">
import { onMounted } from 'vue'
import { Calendar, CircleCheck, DataLine, TrendCharts } from '@element-plus/icons-vue'
import ActivityHeatmap from '@/components/ActivityHeatmap.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import { useStatisticsStore } from '@/stores/statistics'

const store = useStatisticsStore()
onMounted(async () => {
  await Promise.all([store.fetchOverview(), store.fetchActivity()])
})
</script>

<template>
  <div class="page">
    <PageHeader title="活跃统计" subtitle="当天提交过即记为活跃。" />
    <div class="metric-grid">
      <StatCard label="连续活跃" :value="`${store.overview.activeDays} 天`" :icon="Calendar" accent="warning" />
      <StatCard label="今日提交" :value="store.overview.todaySubmissions" :icon="DataLine" accent="primary" />
      <StatCard label="已通过" :value="store.overview.acceptedProblems" :icon="CircleCheck" accent="success" />
      <StatCard label="正确率" :value="`${store.overview.accuracy}%`" :icon="TrendCharts" accent="info" />
    </div>
    <el-card class="section-card">
      <h2 class="card-title">活跃日历</h2>
      <ActivityHeatmap :data="store.activity" :months="3" />
    </el-card>
  </div>
</template>
