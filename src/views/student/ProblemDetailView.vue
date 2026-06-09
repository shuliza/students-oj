<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { EditPen } from '@element-plus/icons-vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import { problemApi } from '@/api'
import type { Problem } from '@/types'

type DescriptionBlock = {
  type: 'paragraph' | 'table'
  text: string
}

const route = useRoute()
const router = useRouter()
const problem = ref<Problem | null>(null)
const isHtmlDescription = computed(() => /<\/?[a-z][\s\S]*>/i.test(problem.value?.description ?? ''))
const descriptionBlocks = computed(() => formatPlainDescription(problem.value?.description ?? ''))

function isTableLine(line: string) {
  const trimmed = line.trim()
  return /^[+|]/.test(trimmed) || /^\|?\s*-{2,}\s*\|/.test(trimmed)
}

function flushBlock(blocks: DescriptionBlock[], type: DescriptionBlock['type'], lines: string[]) {
  const text = lines.join('\n').trim()
  if (text) {
    blocks.push({ type, text })
  }
}

function formatPlainDescription(raw: string): DescriptionBlock[] {
  const normalized = raw.replace(/\r\n/g, '\n').replace(/\r/g, '\n')
  const blocks: DescriptionBlock[] = []
  let paragraph: string[] = []
  let table: string[] = []

  normalized.split('\n').forEach((line) => {
    if (isTableLine(line)) {
      flushBlock(blocks, 'paragraph', paragraph)
      paragraph = []
      table.push(line.trimEnd())
      return
    }

    if (table.length) {
      flushBlock(blocks, 'table', table)
      table = []
    }

    if (!line.trim()) {
      flushBlock(blocks, 'paragraph', paragraph)
      paragraph = []
      return
    }

    paragraph.push(line.trim())
  })

  flushBlock(blocks, 'table', table)
  flushBlock(blocks, 'paragraph', paragraph)
  return blocks
}

onMounted(async () => {
  problem.value = await problemApi.detail(Number(route.params.id))
})
</script>

<template>
  <div v-if="problem" class="page">
    <PageHeader :title="problem.title" :subtitle="`难度：${problem.difficulty} / 通过率：${problem.passRate}%`">
      <template #actions>
        <el-button type="primary" :icon="EditPen" @click="router.push(`/student/editor/${problem.id}`)">打开 SQL 编辑器</el-button>
      </template>
    </PageHeader>
    <el-card class="section-card">
      <h2 class="card-title">题目描述</h2>
      <div v-if="isHtmlDescription" class="problem-html" v-html="problem.description"></div>
      <div v-else class="description-blocks">
        <template v-for="(block, index) in descriptionBlocks" :key="index">
          <pre v-if="block.type === 'table'" class="description-table">{{ block.text }}</pre>
          <p v-else>{{ block.text }}</p>
        </template>
      </div>
      <el-divider />
      <h3>输入结构</h3>
      <el-input :model-value="problem.sampleInput" type="textarea" :rows="3" readonly />
      <h3>期望输出</h3>
      <el-input :model-value="problem.sampleOutput" type="textarea" :rows="3" readonly />
    </el-card>
  </div>
</template>

<style scoped>
.problem-html :deep(pre) {
  padding: 12px;
  overflow: auto;
  border-radius: 6px;
  background: #f6f8fa;
  line-height: 1.7;
}

.problem-html :deep(code) {
  padding: 2px 5px;
  border-radius: 4px;
  background: #eef2f7;
}

.description-blocks {
  display: flex;
  flex-direction: column;
  gap: 12px;
  color: var(--color-text);
  font-size: 15px;
  line-height: 1.75;
}

.description-blocks p {
  margin: 0;
}

.description-table {
  margin: 0;
  padding: 12px;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  color: #172033;
  background: #f8fafc;
  overflow: auto;
  white-space: pre;
  font-family: var(--font-mono);
  font-size: 12px;
  line-height: 1.55;
}
</style>
