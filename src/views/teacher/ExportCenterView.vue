<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/ui/PageHeader.vue'
import { teacherApi } from '@/api'
import type { ClassGroup, User } from '@/types'

const groups = ref<ClassGroup[]>([])
const students = ref<User[]>([])
const exporting = ref(false)

const exportMode = ref<'group' | 'student'>('group')
const dateRange = ref<[string, string] | null>(null)
const form = ref({
  groupId: null as number | null,
  studentId: null as number | null,
  format: 'xlsx'
})

const fetchGroups = async () => {
  groups.value = await teacherApi.groups()
}

const fetchStudents = async () => {
  students.value = await teacherApi.students()
}

const downloadBlob = (blob: Blob, filename: string) => {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}

const exportFile = async () => {
  exporting.value = true
  try {
    if (exportMode.value === 'group') {
      const blob = await teacherApi.exportGrades({
        groupId: form.value.groupId ?? undefined,
        startDate: dateRange.value?.[0] ?? undefined,
        endDate: dateRange.value?.[1] ?? undefined,
        format: form.value.format
      })
      downloadBlob(blob, form.value.format === 'csv' ? 'student-grades.csv' : 'student-grades.xlsx')
    } else {
      if (!form.value.studentId) {
        ElMessage.warning('请选择学生')
        return
      }
      const blob = await teacherApi.exportStudentGrades(form.value.studentId)
      downloadBlob(blob, 'student-grades.xlsx')
    }
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

onMounted(async () => {
  await Promise.all([fetchGroups(), fetchStudents()])
})
</script>

<template>
  <div class="page">
    <PageHeader title="成绩导出" subtitle="按班级、学生分组或单个学生导出成绩，文件直接下载到浏览器。" />
    <div class="grid-main-side">
      <el-card class="section-card">
        <h2 class="card-title">导出配置</h2>
        <el-form :model="form" label-width="100px">
          <el-form-item label="导出方式">
            <el-radio-group v-model="exportMode">
              <el-radio-button value="group">按分组导出</el-radio-button>
              <el-radio-button value="student">按学生导出</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="exportMode === 'group'" label="学生分组">
            <el-select v-model="form.groupId" placeholder="全部分组" clearable style="width: 260px">
              <el-option label="全部分组" :value="null" />
              <el-option v-for="item in groups" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="exportMode === 'student'" label="选择学生">
            <el-select v-model="form.studentId" placeholder="请选择学生" filterable style="width: 260px">
              <el-option v-for="item in students" :key="item.id" :label="`${item.realName} (${item.studentNo})`" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="exportMode === 'group'" label="提交日期">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              value-format="YYYY-MM-DD"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              clearable
              style="width: 260px"
            />
          </el-form-item>
          <el-form-item label="文件格式">
            <el-radio-group v-model="form.format">
              <el-radio-button value="xlsx">Excel</el-radio-button>
              <el-radio-button value="csv">CSV</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="exporting" @click="exportFile">导出成绩</el-button>
          </el-form-item>
        </el-form>
      </el-card>
      <el-card class="section-card">
        <h2 class="card-title">导出内容</h2>
        <el-timeline>
          <el-timeline-item timestamp="学生基础信息">姓名、学号、班级、分组</el-timeline-item>
          <el-timeline-item timestamp="题目成绩">通过状态、最高分、提交次数</el-timeline-item>
          <el-timeline-item timestamp="学习统计">活跃天数、正确率、最近提交</el-timeline-item>
        </el-timeline>
      </el-card>
    </div>
  </div>
</template>
