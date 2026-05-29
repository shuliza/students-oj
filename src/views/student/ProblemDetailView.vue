<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { problemApi } from '@/api'
import type { Problem } from '@/types'

const route = useRoute()
const router = useRouter()
const problem = ref<Problem | null>(null)

onMounted(async () => {
  problem.value = await problemApi.detail(Number(route.params.id))
})
</script>

<template>
  <div v-if="problem" class="page">
    <div class="toolbar">
      <div>
        <h1 class="page-title">{{ problem.title }}</h1>
        <p class="page-subtitle">难度：{{ problem.difficulty }} / 通过率：{{ problem.passRate }}%</p>
      </div>
      <el-button type="primary" @click="router.push(`/student/editor/${problem.id}`)">打开 SQL 编辑器</el-button>
    </div>
    <el-card class="section-card">
      <h2 class="card-title">题目描述</h2>
      <p>{{ problem.description }}</p>
      <el-divider />
      <h3>输入结构</h3>
      <el-input :model-value="problem.sampleInput" type="textarea" :rows="3" readonly />
      <h3>期望输出</h3>
      <el-input :model-value="problem.sampleOutput" type="textarea" :rows="3" readonly />
    </el-card>
  </div>
</template>
