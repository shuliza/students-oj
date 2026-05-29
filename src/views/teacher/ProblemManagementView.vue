<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import StatusTag from '@/components/StatusTag.vue'
import { teacherApi } from '@/api'
import type { Problem } from '@/types'

const problems = ref<Problem[]>([])
const keyword = ref('')

const list = computed(() => problems.value.filter((item) => !keyword.value || item.title.includes(keyword.value)))

onMounted(async () => {
  problems.value = await teacherApi.problems()
})
</script>

<template>
  <div class="page">
    <div class="toolbar">
      <div>
        <h1 class="page-title">题库管理</h1>
        <p class="page-subtitle">维护题目、标签、测试用例和标准答案。</p>
      </div>
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索题目" clearable style="width: 220px" />
        <el-button>导入题目</el-button>
        <el-button type="primary">新增题目</el-button>
      </div>
    </div>
    <el-card class="section-card">
      <el-table :data="list">
        <el-table-column prop="title" label="题目" min-width="220" />
        <el-table-column prop="difficulty" label="难度" width="110" />
        <el-table-column label="标签" min-width="180">
          <template #default="{ row }">
            <el-tag v-for="tag in row.tags" :key="tag" class="tag">{{ tag }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submissions" label="提交数" width="110" />
        <el-table-column label="通过率" width="120"><template #default="{ row }">{{ row.passRate }}%</template></el-table-column>
        <el-table-column label="状态" width="120"><template #default="{ row }"><StatusTag :status="row.status" /></template></el-table-column>
        <el-table-column label="操作" width="220">
          <el-button type="primary" link>编辑</el-button>
          <el-button type="primary" link>测试用例</el-button>
          <el-button type="danger" link>删除</el-button>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.tag {
  margin-right: 6px;
}
</style>
