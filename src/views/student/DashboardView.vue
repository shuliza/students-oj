<script setup lang="ts">
import { computed, onMounted } from 'vue'
import ActivityHeatmap from '@/components/ActivityHeatmap.vue'
import StatusTag from '@/components/StatusTag.vue'
import { useProblemStore } from '@/stores/problem'
import { useStatisticsStore } from '@/stores/statistics'

const problemStore = useProblemStore()
const statisticsStore = useStatisticsStore()

const todayAccuracy = computed(() => {
  const { todayPassed, todayAttempted } = statisticsStore.overview
  if (!todayAttempted) return 0
  return Math.round(todayPassed * 100 / todayAttempted)
})

onMounted(async () => {
  await Promise.all([problemStore.fetchProblems(), problemStore.fetchSubmissions(), statisticsStore.fetchOverview(), statisticsStore.fetchActivity()])
})
</script>

<template>
  <div class="page">
    <div>
      <h1 class="page-title">学习首页</h1>
      <p class="page-subtitle">查看今日练习、最近提交和活跃情况。</p>
    </div>
    <div class="metric-grid">
      <div class="metric-card"><div class="metric-label">今日提交次数</div><div class="metric-value">{{ statisticsStore.overview.todaySubmissions }}</div></div>
      <div class="metric-card"><div class="metric-label">今日通过题目数</div><div class="metric-value">{{ statisticsStore.overview.todayPassed }}</div></div>
      <div class="metric-card"><div class="metric-label">今日正确率</div><div class="metric-value">{{ todayAccuracy }}%</div></div>
      <div class="metric-card"><div class="metric-label">连续活跃</div><div class="metric-value">{{ statisticsStore.overview.activeDays }} 天</div></div>
    </div>
    <div class="grid-2">
      <el-card class="section-card">
        <h2 class="card-title">推荐题目</h2>
        <el-table :data="problemStore.problems" height="280">
          <el-table-column prop="title" label="题目" />
          <el-table-column prop="difficulty" label="难度" width="100" />
          <el-table-column label="状态" width="110">
            <template #default="{ row }"><StatusTag :status="row.status" /></template>
          </el-table-column>
        </el-table>
      </el-card>
      <el-card class="section-card">
        <h2 class="card-title">我的活跃</h2>
        <ActivityHeatmap :data="statisticsStore.activity" :months="1" />
      </el-card>
    </div>
    <el-card class="section-card">
      <h2 class="card-title">最近提交</h2>
      <el-table :data="problemStore.submissions">
        <el-table-column prop="id" label="提交 ID" width="120" />
        <el-table-column prop="problemTitle" label="题目" />
        <el-table-column label="结果" width="130"><template #default="{ row }"><StatusTag :status="row.status" /></template></el-table-column>
        <el-table-column prop="runtimeMs" label="耗时(ms)" width="120" />
        <el-table-column prop="submittedAt" label="提交时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>
