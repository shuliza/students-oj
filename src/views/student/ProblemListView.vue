<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { EditPen, View } from '@element-plus/icons-vue'
import StatusTag from '@/components/ui/StatusTag.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import FilterBar from '@/components/ui/FilterBar.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import { useProblemStore } from '@/stores/problem'

const router = useRouter()
const store = useProblemStore()
const keyword = ref('')
const difficulty = ref('')
const tag = ref('')
const status = ref('')
const sort = ref<'default' | 'passRateDesc' | 'passRateAsc'>('default')
const page = ref(1)
const pageSize = 10

const allTags = computed(() => {
  const set = new Set<string>()
  store.problems.forEach((p) => p.tags?.forEach((t) => set.add(t)))
  return Array.from(set)
})

const filtered = computed(() => {
  let list = store.problems.filter((item) => {
    const hitKeyword = !keyword.value || item.title.includes(keyword.value)
    const hitDifficulty = !difficulty.value || item.difficulty === difficulty.value
    const hitTag = !tag.value || item.tags?.includes(tag.value)
    const hitStatus = !status.value || item.status === status.value
    return hitKeyword && hitDifficulty && hitTag && hitStatus
  })
  if (sort.value === 'passRateDesc') list = [...list].sort((a, b) => b.passRate - a.passRate)
  if (sort.value === 'passRateAsc') list = [...list].sort((a, b) => a.passRate - b.passRate)
  return list
})

const paged = computed(() => {
  const start = (page.value - 1) * pageSize
  return filtered.value.slice(start, start + pageSize)
})

const difficultyLabel = (value: string) =>
  value === 'EASY' ? '简单' : value === 'MEDIUM' ? '中等' : value === 'HARD' ? '困难' : value

onMounted(store.fetchProblems)
</script>

<!-- PLACEHOLDER_TEMPLATE -->
<template>
  <div class="page">
    <PageHeader title="题目列表" subtitle="按难度、标签和完成状态筛选题目。" />

    <FilterBar v-model:keyword="keyword" search-placeholder="搜索题目">
      <el-select v-model="difficulty" placeholder="难度" clearable style="width: 130px">
        <el-option label="简单" value="EASY" />
        <el-option label="中等" value="MEDIUM" />
        <el-option label="困难" value="HARD" />
      </el-select>
      <el-select v-model="tag" placeholder="标签" clearable filterable style="width: 150px">
        <el-option v-for="t in allTags" :key="t" :label="t" :value="t" />
      </el-select>
      <el-select v-model="status" placeholder="状态" clearable style="width: 130px">
        <el-option label="未做过" value="TODO" />
        <el-option label="已通过" value="ACCEPTED" />
        <el-option label="未通过" value="FAILED" />
      </el-select>
      <el-select v-model="sort" placeholder="排序" style="width: 150px">
        <el-option label="默认排序" value="default" />
        <el-option label="通过率从高到低" value="passRateDesc" />
        <el-option label="通过率从低到高" value="passRateAsc" />
      </el-select>
    </FilterBar>

    <el-card class="section-card">
      <el-table v-if="paged.length" :data="paged">
        <el-table-column prop="title" label="题目" min-width="220" show-overflow-tooltip />
        <el-table-column label="难度" width="100">
          <template #default="{ row }">
            <span class="diff" :class="row.difficulty.toLowerCase()">{{ difficultyLabel(row.difficulty) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="标签" min-width="180">
          <template #default="{ row }">
            <el-tag v-for="t in row.tags" :key="t" class="tag" size="small" effect="plain">{{ t }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="passRate" label="通过率" width="100">
          <template #default="{ row }">{{ row.passRate }}%</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }"><StatusTag :status="row.status" /></template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button :icon="View" link type="primary" @click="router.push(`/student/problems/${row.id}`)">详情</el-button>
            <el-button :icon="EditPen" link type="primary" @click="router.push(`/student/editor/${row.id}`)">练习</el-button>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-else description="没有符合条件的题目" />

      <div v-if="filtered.length > pageSize" class="pager">
        <el-pagination
          layout="prev, pager, next, total"
          :total="filtered.length"
          :page-size="pageSize"
          :current-page="page"
          @current-change="(p: number) => (page = p)"
        />
      </div>
    </el-card>
  </div>
</template>
<!-- PLACEHOLDER_TEMPLATE -->

<style scoped>
.tag {
  margin-right: 6px;
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

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-4);
}
</style>
