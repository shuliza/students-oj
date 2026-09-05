<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Calendar, CircleCheck, DataLine, TrendCharts } from '@element-plus/icons-vue'
import ActivityHeatmap from '@/components/ActivityHeatmap.vue'
import StatusTag from '@/components/ui/StatusTag.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import ChartBox from '@/components/ui/ChartBox.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import { useProblemStore } from '@/stores/problem'
import { useStatisticsStore } from '@/stores/statistics'
import { recentActivity } from '@/utils/activity'

const router = useRouter()
const problemStore = useProblemStore()
const statisticsStore = useStatisticsStore()

const todayAccuracy = computed(() => {
  const { todayPassed, todayAttempted } = statisticsStore.overview
  if (!todayAttempted) return 0
  return Math.round((todayPassed * 100) / todayAttempted)
})

const recentProblems = computed(() => problemStore.problems.slice(0, 6))
const recentSubmissions = computed(() => problemStore.submissions.slice(0, 8))

// 近 14 天提交趋势（来自活跃数据）
const trendOption = computed(() => {
  const days = recentActivity(statisticsStore.activity, 14)
  return {
    grid: { left: 8, right: 12, top: 24, bottom: 24, containLabel: true },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: days.map((d) => d.date.slice(5)),
      axisLine: { lineStyle: { color: 'var(--color-border)' } },
      axisLabel: { color: 'var(--color-text)' }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'var(--color-border)' } },
      axisLabel: { color: 'var(--color-text)' }
    },
    series: [
      {
        name: '提交',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        data: days.map((d) => d.count),
        itemStyle: { color: 'var(--color-primary)' },
        lineStyle: { width: 3, color: 'var(--color-primary)' },
        areaStyle: { opacity: 0.14, color: 'var(--color-primary)' }
      }
    ]
  }
})

onMounted(async () => {
  await Promise.all([
    problemStore.fetchProblems(),
    problemStore.fetchSubmissions(),
    statisticsStore.fetchOverview(),
    statisticsStore.fetchActivity()
  ])
})
</script>

<!-- PLACEHOLDER_TEMPLATE -->
<template>
  <div class="page">
    <PageHeader title="学习首页" subtitle="查看今日练习、最近提交和活跃情况。" />

    <div class="metric-grid">
      <StatCard label="今日提交次数" :value="statisticsStore.overview.todaySubmissions" :icon="DataLine" accent="primary" />
      <StatCard label="今日通过题目" :value="statisticsStore.overview.todayPassed" :icon="CircleCheck" accent="success" />
      <StatCard label="今日正确率" :value="`${todayAccuracy}%`" :icon="TrendCharts" accent="info" />
      <StatCard label="连续活跃" :value="`${statisticsStore.overview.activeDays} 天`" :icon="Calendar" accent="warning" />
    </div>

    <div class="grid-main-side">
      <el-card class="section-card">
        <h2 class="card-title">近 14 天提交趋势</h2>
        <ChartBox :option="trendOption" height="300px" />
      </el-card>
      <el-card class="section-card">
        <h2 class="card-title">我的活跃</h2>
        <ActivityHeatmap :data="statisticsStore.activity" :months="1" />
      </el-card>
    </div>

    <div class="grid-2">
      <el-card class="section-card">
        <h2 class="card-title">推荐题目</h2>
        <div class="problem-list">
          <button
            v-for="item in recentProblems"
            :key="item.id"
            class="problem-row"
            type="button"
            @click="router.push(`/student/editor/${item.id}`)"
          >
            <span class="problem-name">{{ item.title }}</span>
            <span class="diff" :class="item.difficulty.toLowerCase()">
              {{ item.difficulty === 'EASY' ? '简单' : item.difficulty === 'MEDIUM' ? '中等' : '困难' }}
            </span>
            <StatusTag :status="item.status" />
          </button>
          <EmptyState v-if="!recentProblems.length" description="暂无题目" min-height="180px" />
        </div>
      </el-card>

      <el-card class="section-card">
        <h2 class="card-title">最近提交</h2>
        <el-table v-if="recentSubmissions.length" :data="recentSubmissions">
          <el-table-column prop="problemTitle" label="题目" min-width="160" show-overflow-tooltip />
          <el-table-column label="结果" width="140">
            <template #default="{ row }"><StatusTag :status="row.status" /></template>
          </el-table-column>
          <el-table-column prop="runtimeMs" label="耗时(ms)" width="100" />
          <el-table-column prop="submittedAt" label="提交时间" min-width="160" />
        </el-table>
        <EmptyState v-else description="还没有提交记录，去做第一题吧" min-height="180px" />
      </el-card>
    </div>
  </div>
</template>
<!-- PLACEHOLDER_TEMPLATE -->

<style scoped>
.problem-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.problem-row {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  width: 100%;
  padding: var(--space-3) var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  cursor: pointer;
  text-align: left;
}

.problem-row:hover {
  border-color: var(--color-primary);
  background: var(--color-surface-2);
}

.problem-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--color-text);
  font-weight: 600;
  font-size: var(--text-base);
}

.diff {
  height: 22px;
  padding: 0 var(--space-2);
  display: inline-flex;
  align-items: center;
  border-radius: var(--radius-pill);
  font-size: var(--text-xs);
  font-weight: 650;
}

.diff.easy { color: var(--color-success); background: var(--color-success-soft); }
.diff.medium { color: var(--color-warning); background: var(--color-warning-soft); }
.diff.hard { color: var(--color-danger); background: var(--color-danger-soft); }
</style>
