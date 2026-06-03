<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import ActivityHeatmap from '@/components/ActivityHeatmap.vue'
import StatusTag from '@/components/StatusTag.vue'
import { statisticsApi, submissionApi, teacherApi } from '@/api'
import { useProblemStore } from '@/stores/problem'
import { useStatisticsStore } from '@/stores/statistics'
import type { ClassGroup, StudentTodaySolved, User } from '@/types'

const problemStore = useProblemStore()
const statisticsStore = useStatisticsStore()

const students = ref<User[]>([])
const groups = ref<ClassGroup[]>([])
const selectedGroup = ref('')
const selectedStudentId = ref<number | ''>('')
const todaySolvedList = ref<StudentTodaySolved[]>([])
const rejudgingId = ref<number | null>(null)

const groupOptions = computed(() => {
  const names = new Set<string>()
  groups.value.forEach((item) => names.add(item.name))
  students.value.forEach((item) => {
    if (item.groupName) names.add(item.groupName)
  })
  return Array.from(names)
})

const studentsInGroup = computed(() =>
  students.value.filter((item) => !selectedGroup.value || item.groupName === selectedGroup.value)
)

const scopeName = computed(() => {
  if (!selectedGroup.value) return '全部班级'
  const selectedStudent = studentsInGroup.value.find((item) => item.id === selectedStudentId.value)
  return selectedStudent ? `${selectedGroup.value} / ${selectedStudent.realName}` : selectedGroup.value
})

watch(selectedGroup, async () => {
  selectedStudentId.value = ''
  await loadScopedData()
})

watch(selectedStudentId, async () => {
  await loadScopedData()
})

onMounted(async () => {
  const [studentList, groupList] = await Promise.all([
    teacherApi.students(),
    teacherApi.groups(),
    loadScopedData()
  ])
  students.value = studentList
  groups.value = groupList
})

async function loadScopedData() {
  const params = selectedGroup.value
    ? {
        groupName: selectedGroup.value,
        studentId: selectedStudentId.value === '' ? undefined : selectedStudentId.value
      }
    : undefined

  await Promise.all([
    statisticsStore.fetchOverview(params),
    statisticsStore.fetchActivity(params),
    problemStore.fetchSubmissions(params),
    statisticsApi.teacherTodaySolved(params).then((data) => {
      todaySolvedList.value = data
    })
  ])
}

async function rejudge(row: { id: number }) {
  rejudgingId.value = row.id
  try {
    const result = await submissionApi.rejudge(row.id)
    ElMessage.success(`重判完成：${result.status}`)
    await loadScopedData()
  } catch {
    // 错误信息由 http 拦截器统一提示
  } finally {
    rejudgingId.value = null
  }
}
</script>

<template>
  <div class="page">
    <div class="toolbar">
      <div>
        <h1 class="page-title">教学数据看板</h1>
        <p class="page-subtitle">班级、题库、提交与通过率概览。</p>
      </div>
      <div class="toolbar">
        <el-select v-model="selectedGroup" placeholder="班级" clearable style="width: 180px">
          <el-option v-for="name in groupOptions" :key="name" :label="name" :value="name" />
        </el-select>
        <el-select
          v-model="selectedStudentId"
          placeholder="学生"
          clearable
          filterable
          :disabled="!selectedGroup"
          style="width: 180px"
        >
          <el-option
            v-for="student in studentsInGroup"
            :key="student.id"
            :label="`${student.realName} ${student.studentNo ?? ''}`"
            :value="student.id"
          />
        </el-select>
      </div>
    </div>

    <div class="scope-line">当前范围：{{ scopeName }}</div>

    <div class="metric-grid">
      <div class="metric-card"><div class="metric-label">学生数量</div><div class="metric-value">{{ statisticsStore.overview.students }}</div></div>
      <div class="metric-card"><div class="metric-label">题目数量</div><div class="metric-value">{{ statisticsStore.overview.problems }}</div></div>
      <div class="metric-card"><div class="metric-label">今日做题数量</div><div class="metric-value">{{ statisticsStore.overview.todaySubmissions }}</div></div>
      <div class="metric-card"><div class="metric-label">提交次数</div><div class="metric-value">{{ statisticsStore.overview.submissions.toLocaleString() }}</div></div>
      <div class="metric-card"><div class="metric-label">平均通过率</div><div class="metric-value">{{ statisticsStore.overview.passRate }}%</div></div>
    </div>

    <div class="grid-2">
      <el-card class="section-card">
        <h2 class="card-title">{{ selectedStudentId === '' ? '班级活跃图' : '学生活跃图' }}</h2>
        <ActivityHeatmap :data="statisticsStore.activity" :months="3" />
      </el-card>
      <el-card class="section-card">
        <h2 class="card-title">最近提交</h2>
        <el-table :data="problemStore.submissions" height="280" empty-text="暂无提交记录">
          <el-table-column prop="userName" label="学生" width="100" />
          <el-table-column prop="problemTitle" label="题目" />
          <el-table-column label="结果" width="120"><template #default="{ row }"><StatusTag :status="row.status" /></template></el-table-column>
          <el-table-column prop="runtimeMs" label="耗时" width="80" />
          <el-table-column label="操作" width="90">
            <template #default="{ row }">
              <el-button type="primary" link size="small" :loading="rejudgingId === row.id" @click="rejudge(row)">重判</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <el-card class="section-card">
      <h2 class="card-title">学生今日做题数量</h2>
      <el-table :data="todaySolvedList" empty-text="暂无学生数据">
        <el-table-column prop="studentNo" label="学号" width="140" />
        <el-table-column prop="realName" label="学生" width="120" />
        <el-table-column prop="groupName" label="班级" />
        <el-table-column prop="todaySolved" label="今日通过新题数" width="160" />
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.scope-line {
  margin-top: -8px;
  color: var(--color-muted);
  font-size: 14px;
}
</style>
