<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import StatusTag from '@/components/StatusTag.vue'
import { useProblemStore } from '@/stores/problem'

const router = useRouter()
const store = useProblemStore()
const keyword = ref('')
const difficulty = ref('')

const list = computed(() =>
  store.problems.filter((item) => {
    const hitKeyword = !keyword.value || item.title.includes(keyword.value)
    const hitDifficulty = !difficulty.value || item.difficulty === difficulty.value
    return hitKeyword && hitDifficulty
  })
)

onMounted(store.fetchProblems)
</script>

<template>
  <div class="page">
    <div class="toolbar">
      <div>
        <h1 class="page-title">题目列表</h1>
        <p class="page-subtitle">按难度、标签和完成状态筛选题目。</p>
      </div>
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索题目" clearable style="width: 220px" />
        <el-select v-model="difficulty" placeholder="难度" clearable style="width: 140px">
          <el-option label="简单" value="EASY" />
          <el-option label="中等" value="MEDIUM" />
          <el-option label="困难" value="HARD" />
        </el-select>
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
        <el-table-column prop="passRate" label="通过率" width="110">
          <template #default="{ row }">{{ row.passRate }}%</template>
        </el-table-column>
        <el-table-column label="状态" width="120"><template #default="{ row }"><StatusTag :status="row.status" /></template></el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" link @click="router.push(`/student/problems/${row.id}`)">详情</el-button>
            <el-button type="primary" link @click="router.push(`/student/editor/${row.id}`)">练习</el-button>
          </template>
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
