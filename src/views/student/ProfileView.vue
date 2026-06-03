<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/api'

const auth = useAuthStore()

const profileForm = reactive({
  realName: auth.user?.realName ?? '',
  email: auth.user?.email ?? ''
})
const profileSaving = ref(false)

const saveProfile = async () => {
  profileSaving.value = true
  try {
    const updated = await authApi.updateProfile({
      realName: profileForm.realName,
      email: profileForm.email
    })
    auth.user = updated
    localStorage.setItem('user', JSON.stringify(updated))
    ElMessage.success('资料已更新')
  } catch {
    ElMessage.error('更新失败')
  } finally {
    profileSaving.value = false
  }
}

const pwdFormRef = ref<FormInstance>()
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdSaving = ref(false)

const pwdRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码长度至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_r, value, cb) =>
        value === pwdForm.newPassword ? cb() : cb(new Error('两次输入的密码不一致')),
      trigger: 'blur'
    }
  ]
}

const submitPassword = async () => {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid) => {
    if (!valid) return
    pwdSaving.value = true
    try {
      await authApi.changePassword(pwdForm.oldPassword, pwdForm.newPassword)
      ElMessage.success('密码已修改')
      pwdFormRef.value?.resetFields()
    } catch {
      // 错误信息由 http 拦截器统一提示
    } finally {
      pwdSaving.value = false
    }
  })
}
</script>

<template>
  <div class="page">
    <div>
      <h1 class="page-title">个人中心</h1>
      <p class="page-subtitle">账号信息、班级与学习状态。</p>
    </div>
    <el-card class="section-card">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="姓名">{{ auth.user?.realName }}</el-descriptions-item>
        <el-descriptions-item label="账号">{{ auth.user?.username }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ auth.user?.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ auth.user?.groupName }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ auth.user?.email }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ auth.user?.status }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <div class="grid-2">
      <el-card class="section-card">
        <h2 class="card-title">编辑资料</h2>
        <el-form :model="profileForm" label-width="80px">
          <el-form-item label="姓名">
            <el-input v-model="profileForm.realName" placeholder="请输入姓名" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="profileSaving" @click="saveProfile">保存资料</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card class="section-card">
        <h2 class="card-title">修改密码</h2>
        <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少 6 位" />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="pwdSaving" @click="submitPassword">修改密码</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>
