<script setup lang="ts">
import { onMounted, ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/ui/PageHeader.vue'
import { teacherApi } from '@/api'
import type { User } from '@/types'

const teachers = ref<User[]>([])
const keyword = ref('')

const createVisible = ref(false)
const createSaving = ref(false)
const createForm = reactive({ username: '', realName: '', email: '', password: '123456' })

const list = computed(() =>
  teachers.value.filter((item) => {
    if (!keyword.value) return true
    const kw = keyword.value.trim()
    return item.realName.includes(kw) || item.username.includes(kw) || (item.email ?? '').includes(kw)
  })
)

const fetchTeachers = async () => {
  teachers.value = await teacherApi.teachers()
}

const openCreate = () => {
  createForm.username = ''
  createForm.realName = ''
  createForm.email = ''
  createForm.password = '123456'
  createVisible.value = true
}

const submitCreate = async () => {
  if (!createForm.username.trim()) {
    ElMessage.warning('请输入账号')
    return
  }
  if (!createForm.realName.trim()) {
    ElMessage.warning('请输入姓名')
    return
  }
  if (createForm.password.trim() && createForm.password.trim().length < 6) {
    ElMessage.warning('密码长度至少 6 位')
    return
  }
  createSaving.value = true
  try {
    await teacherApi.createTeacher({
      username: createForm.username.trim(),
      realName: createForm.realName.trim(),
      email: createForm.email.trim() || undefined,
      password: createForm.password.trim() || undefined
    })
    ElMessage.success('教师账号已创建')
    createVisible.value = false
    await fetchTeachers()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '创建失败，账号可能已存在')
  } finally {
    createSaving.value = false
  }
}

const toggleStatus = async (row: User) => {
  const next = row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  const action = next === 'DISABLED' ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确认${action}教师「${row.realName}」？`, `${action}确认`, { type: 'warning' })
    await teacherApi.updateTeacherStatus(row.id, next)
    ElMessage.success(`已${action}`)
    await fetchTeachers()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const resetPassword = async (row: User) => {
  try {
    const { value } = await ElMessageBox.prompt(
      `为教师「${row.realName}」设置新密码（留空则重置为 123456）`,
      '重置密码',
      { inputPlaceholder: '至少 6 位', inputType: 'password', confirmButtonText: '确认', cancelButtonText: '取消' }
    )
    await teacherApi.resetTeacherPassword(row.id, value?.trim() || undefined)
    ElMessage.success('密码已重置')
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error('重置失败')
  }
}

onMounted(fetchTeachers)
</script>

<template>
  <div class="page">
    <PageHeader title="教师管理" subtitle="为新入职的教师创建登录账号，管理教师状态与密码。" />
    <div class="toolbar toolbar-controls">
      <el-input v-model="keyword" placeholder="姓名 / 账号 / 邮箱" clearable style="width: 240px" />
      <el-button type="primary" @click="openCreate">添加教师</el-button>
    </div>
    <el-card class="section-card">
      <el-table :data="list">
        <el-table-column prop="realName" label="姓名" width="140" />
        <el-table-column prop="username" label="账号" width="160" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button :type="row.status === 'ACTIVE' ? 'warning' : 'success'" link size="small" @click="toggleStatus(row)">
              {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
            </el-button>
            <el-button type="info" link size="small" @click="resetPassword(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="createVisible" title="添加教师" width="480px" destroy-on-close>
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="账号" required>
          <el-input v-model="createForm.username" placeholder="请输入登录账号" />
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="createForm.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="createForm.email" placeholder="选填" />
        </el-form-item>
        <el-form-item label="初始密码">
          <el-input v-model="createForm.password" placeholder="留空使用 123456" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="createSaving" @click="submitCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar-controls {
  justify-content: flex-start;
}
</style>
