<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { teacherApi } from '@/api'
import type { ClassGroup, User } from '@/types'

const students = ref<User[]>([])
const groups = ref<ClassGroup[]>([])
const keyword = ref('')
const group = ref('')
const importing = ref(false)

const list = computed(() =>
  students.value.filter((item) => {
    const hitKeyword = !keyword.value || item.realName.includes(keyword.value) || item.studentNo?.includes(keyword.value)
    const hitGroup = !group.value || item.groupName === group.value
    return hitKeyword && hitGroup
  })
)

const fetchStudents = async () => {
  students.value = await teacherApi.students()
}

const fetchGroups = async () => {
  groups.value = await teacherApi.groups()
}

const handleTemplateExport = async () => {
  try {
    const blob = await teacherApi.exportStudentTemplate()
    downloadBlob(blob, 'student-template.xlsx')
    ElMessage.success('模板已下载')
  } catch {
    ElMessage.error('模板下载失败')
  }
}

const handleBatchImport = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.xlsx,.xls,.csv'
  input.onchange = async () => {
    const file = input.files?.[0]
    if (!file) return
    importing.value = true
    try {
      const result = await teacherApi.importStudents(file)
      ElMessage.success(`成功导入 ${result.imported} 名学生`)
      await fetchStudents()
    } catch {
      ElMessage.error('导入失败')
    } finally {
      importing.value = false
    }
  }
  input.click()
}

const downloadBlob = (blob: Blob, filename: string) => {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}

onMounted(async () => {
  await Promise.all([fetchStudents(), fetchGroups()])
})
</script>

<template>
  <div class="page">
    <div class="toolbar">
      <div>
        <h1 class="page-title">学生管理</h1>
        <p class="page-subtitle">查看学生账号、班级分组和学习状态。</p>
      </div>
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="姓名 / 学号" clearable style="width: 220px" />
        <el-select v-model="group" placeholder="班级" clearable style="width: 160px">
          <el-option v-for="item in groups" :key="item.id" :label="item.name" :value="item.name" />
        </el-select>
        <el-button @click="handleTemplateExport">模板导出</el-button>
        <el-button type="primary" :loading="importing" @click="handleBatchImport">批量导入</el-button>
      </div>
    </div>
    <el-card class="section-card">
      <el-table :data="list">
        <el-table-column prop="studentNo" label="学号" width="140" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="username" label="账号" width="140" />
        <el-table-column prop="groupName" label="分组" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }"><el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status }}</el-tag></template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
