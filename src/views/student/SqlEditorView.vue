<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import { ArrowLeft, ArrowRight, Document, MagicStick, Operation, RefreshRight, Upload, VideoPlay } from '@element-plus/icons-vue'
import SqlMonacoEditor from '@/components/SqlMonacoEditor.vue'
import StatusTag from '@/components/ui/StatusTag.vue'
import { aiApi, problemApi, submissionApi } from '@/api'
import { useProblemStore } from '@/stores/problem'
import type { Problem, Submission } from '@/types'

type SchemaTable = {
  name: string
  columns: string[]
}

type DescriptionBlock = {
  type: 'paragraph' | 'table'
  text: string
}

type RunResult = {
  status: string
  runtimeMs: number
  message: string
  match: boolean
  columns: string[]
  rows: Record<string, unknown>[]
}

const route = useRoute()
const router = useRouter()
const problemStore = useProblemStore()
const problem = ref<Problem | null>(null)
const DEFAULT_SQL = 'SELECT s.name, c.course, c.score\nFROM student s\nJOIN score c ON s.id = c.student_id\nWHERE c.score >= 80;'
const sql = ref(DEFAULT_SQL)
const result = ref<Submission | null>(null)
const suggestion = ref('')
const judging = ref(false)
const running = ref(false)
const aiLoading = ref(false)
const loadingProblem = ref(false)
const activeProblemTab = ref('description')
const activeBottomTab = ref('cases')
const runResult = ref<RunResult | null>(null)
const mySubmissions = ref<Submission[]>([])
const aiHistory = ref<Array<{ suggestion: string; createdAt?: string }>>([])

const problemList = computed(() => problemStore.problems)
const currentProblemIndex = computed(() => problemList.value.findIndex((item) => item.id === problem.value?.id))
const previousProblem = computed(() => {
  const index = currentProblemIndex.value
  return index > 0 ? problemList.value[index - 1] : null
})
const nextProblem = computed(() => {
  const index = currentProblemIndex.value
  return index >= 0 && index < problemList.value.length - 1 ? problemList.value[index + 1] : null
})
const problemProgress = computed(() => {
  const index = currentProblemIndex.value
  return index >= 0 ? `${index + 1} / ${problemList.value.length}` : ''
})
const schemaTables = computed(() => parseSchema(problem.value?.sampleInput ?? ''))
const publicCaseInput = computed(() => normalizeCaseText(problem.value?.sampleInput ?? ''))
const publicCaseOutput = computed(() => normalizeCaseText(problem.value?.sampleOutput ?? ''))
const hasPublicCase = computed(() => Boolean(publicCaseInput.value || publicCaseOutput.value))
const isHtmlDescription = computed(() => /<\/?[a-z][\s\S]*>/i.test(problem.value?.description ?? ''))
const descriptionBlocks = computed(() => formatPlainDescription(problem.value?.description ?? ''))
const statusTone = computed(() => {
  const status = result.value?.status ?? runResult.value?.status
  if (!status) return 'idle'
  if (status === 'ACCEPTED') return 'accepted'
  if (status === 'PENDING') return 'pending'
  return 'failed'
})

const submit = async () => {
  if (!problem.value) return
  judging.value = true
  suggestion.value = ''
  activeBottomTab.value = 'result'
  try {
    result.value = await problemApi.submit(problem.value.id, sql.value)
    ElNotification.info({ title: '判题中', message: '提交已进入队列，正在等待结果。' })
    result.value = await pollSubmission(result.value.id)
    ElMessage.success('判题完成')
  } finally {
    judging.value = false
  }
}

const requestAiSuggestion = async () => {
  if (!problem.value || !result.value) return
  aiLoading.value = true
  activeBottomTab.value = 'result'
  try {
    const response = await aiApi.requestSuggestion({
      submissionId: result.value.id,
      problemId: problem.value.id,
      status: result.value.status,
      errorMessage: result.value.message,
      studentSql: sql.value
    })
    suggestion.value = response.suggestion
    await loadAiHistory()
  } finally {
    aiLoading.value = false
  }
}

const pollSubmission = async (submissionId: number) => {
  const terminalStatuses = ['ACCEPTED', 'WRONG_ANSWER', 'TIME_LIMIT', 'RUNTIME_ERROR', 'WA', 'TLE', 'RE']
  for (let index = 0; index < 20; index += 1) {
    const latest = await submissionApi.get(submissionId)
    if (terminalStatuses.includes(latest.status)) {
      return latest
    }
    await new Promise((resolve) => window.setTimeout(resolve, 1500))
  }
  return submissionApi.get(submissionId)
}

const formatSql = () => {
  const keywords = [
    'select',
    'from',
    'where',
    'join',
    'left join',
    'right join',
    'inner join',
    'group by',
    'order by',
    'having',
    'limit',
    'rank',
    'over',
    'partition by'
  ]
  let value = sql.value.trim()
  keywords.forEach((keyword) => {
    const pattern = new RegExp(`\\b${keyword}\\b`, 'gi')
    value = value.replace(pattern, keyword.toUpperCase())
  })
  sql.value = value
}

const runOnly = async () => {
  if (!problem.value || !sql.value.trim()) {
    ElMessage.warning('请先编写 SQL')
    return
  }
  running.value = true
  activeBottomTab.value = 'result'
  try {
    runResult.value = await problemApi.run(problem.value.id, sql.value)
    if (runResult.value.match) {
      ElMessage.success('试运行通过，结果与参考答案一致')
    } else if (runResult.value.status === 'ACCEPTED' || runResult.value.status === 'WRONG_ANSWER') {
      ElMessage.warning('试运行完成，结果与参考答案不一致')
    } else {
      ElMessage.error(runResult.value.message || '试运行未通过')
    }
  } catch {
    ElMessage.error('试运行失败，请稍后重试')
  } finally {
    running.value = false
  }
}

const resetSql = () => {
  sql.value = DEFAULT_SQL
  runResult.value = null
  ElMessage.info('已重置为初始 SQL')
}

const loadMySubmissions = async () => {
  if (!problem.value) return
  const all = await submissionApi.mine()
  mySubmissions.value = all.filter((s) => s.problemId === problem.value!.id)
}

const loadAiHistory = async () => {
  if (!problem.value) return
  aiHistory.value = await aiApi.history(problem.value.id)
}

const goBack = () => {
  router.push('/student/problems')
}

const switchProblem = (target: Problem | null) => {
  if (!target || loadingProblem.value) return
  router.push(`/student/editor/${target.id}`)
}

const resetProblemState = () => {
  sql.value = DEFAULT_SQL
  result.value = null
  suggestion.value = ''
  runResult.value = null
  mySubmissions.value = []
  aiHistory.value = []
  activeProblemTab.value = 'description'
  activeBottomTab.value = 'cases'
}

const loadProblem = async (id: number) => {
  loadingProblem.value = true
  resetProblemState()
  try {
    problem.value = await problemApi.detail(id)
    await Promise.all([loadMySubmissions(), loadAiHistory()])
  } finally {
    loadingProblem.value = false
  }
}

function parseSchema(input: string): SchemaTable[] {
  const matches = input.matchAll(/([\w\u4e00-\u9fa5]+)\(([^)]*)\)/g)
  return Array.from(matches, (match) => ({
    name: match[1],
    columns: match[2].split(',').map((item) => item.trim()).filter(Boolean)
  }))
}

function normalizeCaseText(value: string) {
  const trimmed = value.replace(/\r\n/g, '\n').replace(/\r/g, '\n').trim()
  return trimmed === '请参考题目描述' ? '' : trimmed
}

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
  if (!problemStore.problems.length) {
    await problemStore.fetchProblems()
  }
  await loadProblem(Number(route.params.id ?? 101))
})

watch(
  () => route.params.id,
  async (id) => {
    const nextId = Number(id ?? 101)
    if (!Number.isNaN(nextId) && nextId !== problem.value?.id) {
      await loadProblem(nextId)
    }
  }
)
</script>

<template>
  <div v-if="problem" class="leetcode-sql">
    <header class="topbar">
      <div class="topbar-left">
        <el-button class="icon-button" :icon="ArrowLeft" circle text @click="goBack" />
        <span class="brand-mark">SQL OJ</span>
        <span class="nav-item active">数据库</span>
        <span class="nav-item">题库</span>
        <span class="nav-item">讨论</span>
      </div>
      <div class="topbar-actions">
        <div class="problem-switcher">
          <el-button
            class="switch-button"
            :icon="ArrowLeft"
            :disabled="!previousProblem || loadingProblem"
            :loading="loadingProblem"
            @click="switchProblem(previousProblem)"
          >
            上一题
          </el-button>
          <span v-if="problemProgress" class="problem-progress">{{ problemProgress }}</span>
          <el-button
            class="switch-button"
            :icon="ArrowRight"
            :disabled="!nextProblem || loadingProblem"
            @click="switchProblem(nextProblem)"
          >
            下一题
          </el-button>
        </div>
        <el-button class="ghost-action" :icon="RefreshRight" text @click="resetSql">重置</el-button>
        <el-button class="run-button" :icon="VideoPlay" :loading="running" :disabled="judging" @click="runOnly">运行</el-button>
        <el-button class="submit-button" :icon="Upload" :loading="judging" @click="submit">提交</el-button>
      </div>
    </header>

    <main class="workspace">
      <section class="problem-pane">
        <div class="pane-tabs">
          <button :class="{ active: activeProblemTab === 'description' }" @click="activeProblemTab = 'description'">
            <el-icon><Document /></el-icon>
            题目描述
          </button>
          <button :class="{ active: activeProblemTab === 'solution' }" @click="activeProblemTab = 'solution'">题解</button>
          <button :class="{ active: activeProblemTab === 'submissions' }" @click="activeProblemTab = 'submissions'">提交记录</button>
        </div>

        <div class="problem-scroll">
          <template v-if="activeProblemTab === 'description'">
            <h1>{{ problem.id }}. {{ problem.title }}</h1>
            <div class="meta-row">
              <span class="difficulty" :class="problem.difficulty.toLowerCase()">
                {{ problem.difficulty === 'EASY' ? '简单' : problem.difficulty === 'MEDIUM' ? '中等' : '困难' }}
              </span>
              <span v-for="tag in problem.tags" :key="tag" class="tag">{{ tag }}</span>
              <span class="pass-rate">通过率 {{ problem.passRate }}%</span>
            </div>

            <div v-if="isHtmlDescription" class="description problem-html" v-html="problem.description"></div>
            <div v-else class="description description-blocks">
              <template v-for="(block, index) in descriptionBlocks" :key="index">
                <pre v-if="block.type === 'table'" class="description-table">{{ block.text }}</pre>
                <p v-else>{{ block.text }}</p>
              </template>
            </div>
            <p class="return-note">以任意顺序返回结果表。</p>

            <section class="schema-section">
              <h2>SQL Schema</h2>
              <div v-for="table in schemaTables" :key="table.name" class="schema-table">
                <div class="schema-name">表：{{ table.name }}</div>
                <table>
                  <thead>
                    <tr>
                      <th>列名</th>
                      <th>类型</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="column in table.columns" :key="column">
                      <td>{{ column }}</td>
                      <td>varchar / int</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </section>

            <section class="example-section">
              <h2>结果格式</h2>
              <pre>{{ problem.sampleOutput }}</pre>
            </section>
          </template>

          <div v-else-if="activeProblemTab === 'solution'" class="tab-list">
            <template v-if="aiHistory.length">
              <div v-for="(item, index) in aiHistory" :key="index" class="ai-history-item">
                <div class="ai-history-time">
                  <el-icon><MagicStick /></el-icon>
                  <span>{{ item.createdAt || 'AI 建议' }}</span>
                </div>
                <p class="ai-history-text">{{ item.suggestion }}</p>
              </div>
            </template>
            <div v-else class="empty-state">
              <el-icon><MagicStick /></el-icon>
              <span>完成一次提交后，点击「AI 建议」即可在此查看历史建议。</span>
            </div>
          </div>

          <div v-else class="tab-list">
            <template v-if="mySubmissions.length">
              <div v-for="item in mySubmissions" :key="item.id" class="submission-item">
                <StatusTag :status="item.status" />
                <span class="submission-score">{{ item.score }} 分</span>
                <span class="submission-runtime">{{ item.runtimeMs }}ms</span>
                <span class="submission-time">{{ item.submittedAt }}</span>
              </div>
            </template>
            <div v-else class="empty-state">
              <el-icon><Operation /></el-icon>
              <span>本题暂无提交记录，正式提交后会同步更新。</span>
            </div>
          </div>
        </div>
      </section>

      <section class="coding-pane">
        <div class="code-panel">
          <div class="panel-titlebar">
            <div class="title-left">
              <span class="status-dot"></span>
              <strong>代码</strong>
            </div>
            <div class="title-tools">
              <el-select model-value="MySQL" size="small" class="language-select">
                <el-option label="MySQL" value="MySQL" />
              </el-select>
              <span class="smart-mode">智能模式</span>
              <el-button size="small" text @click="formatSql">格式化</el-button>
            </div>
          </div>
          <SqlMonacoEditor v-model="sql" height="100%" />
        </div>

        <div class="bottom-panel">
          <div class="pane-tabs compact">
            <button :class="{ active: activeBottomTab === 'cases' }" @click="activeBottomTab = 'cases'">测试用例</button>
            <button :class="{ active: activeBottomTab === 'result' }" @click="activeBottomTab = 'result'">测试结果</button>
          </div>

          <div v-if="activeBottomTab === 'cases'" class="case-body">
            <div class="case-pill">Case 1</div>
            <template v-if="hasPublicCase">
              <div v-if="publicCaseInput" class="case-block">
                <div class="case-title">输入</div>
                <pre>{{ publicCaseInput }}</pre>
              </div>
              <div v-if="publicCaseOutput" class="case-block">
                <div class="case-title">预期输出</div>
                <pre>{{ publicCaseOutput }}</pre>
              </div>
            </template>
            <template v-else>
              <div v-for="table in schemaTables" :key="table.name" class="case-block">
                <div class="case-title">{{ table.name }} =</div>
                <pre>| {{ table.columns.join(' | ') }} |
| {{ table.columns.map(() => '--------').join(' | ') }} |</pre>
              </div>
            </template>
          </div>

          <div v-else class="result-body" :class="statusTone">
            <el-empty v-if="!result && !runResult" description="暂无运行结果" />
            <template v-else-if="runResult && !result">
              <div class="result-head">
                <StatusTag :status="runResult.status" />
                <strong>{{ runResult.match ? 100 : 0 }} 分</strong>
                <span>{{ runResult.runtimeMs }}ms</span>
              </div>
              <p v-if="runResult.message" class="result-message">{{ runResult.message }}</p>
              <el-table
                v-if="runResult.columns.length"
                :data="runResult.rows"
                size="small"
                border
                class="run-result-table"
              >
                <el-table-column
                  v-for="column in runResult.columns"
                  :key="column"
                  :prop="column"
                  :label="column"
                  min-width="120"
                  show-overflow-tooltip
                />
              </el-table>
            </template>
            <template v-else-if="result">
              <div class="result-head">
                <StatusTag :status="result.status" />
                <strong>{{ result.score }} 分</strong>
                <span>{{ result.runtimeMs }}ms</span>
              </div>
              <p v-if="result.message" class="result-message">{{ result.message }}</p>
              <el-button
                class="ai-button"
                type="success"
                :loading="aiLoading"
                :disabled="result.status === 'PENDING'"
                @click="requestAiSuggestion"
              >
                AI 建议
              </el-button>
              <el-alert v-if="suggestion" :title="suggestion" type="success" show-icon :closable="false" />
            </template>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<style scoped>
.leetcode-sql {
  --workspace-bg: #0b1120;
  --workspace-surface: #111827;
  --workspace-surface-2: #162033;
  --workspace-surface-3: #1f2937;
  --workspace-border: #29364d;
  --workspace-text: #f8fafc;
  --workspace-muted: #94a3b8;
  --workspace-accent: #22c55e;
  height: 100vh;
  color: var(--workspace-text);
  background:
    radial-gradient(circle at 80% 0, rgba(34, 197, 94, 0.1), transparent 320px),
    var(--workspace-bg);
  overflow: hidden;
}

.topbar {
  height: 46px;
  padding: 0 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--workspace-border);
  background: rgba(15, 23, 42, 0.94);
  backdrop-filter: blur(14px);
}

.topbar-left,
.topbar-actions,
.title-left,
.title-tools,
.meta-row,
.result-head {
  display: flex;
  align-items: center;
}

.topbar-left {
  gap: 10px;
}

.topbar-actions {
  gap: 8px;
}

.problem-switcher {
  height: 32px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding-right: 8px;
  margin-right: 2px;
  border-right: 1px solid var(--workspace-border);
}

.switch-button {
  height: 28px;
  min-width: 76px;
  border: 1px solid var(--workspace-border);
  border-radius: 6px;
  color: #dbeafe;
  background: #172033;
  font-weight: 600;
}

.problem-progress {
  min-width: 48px;
  color: var(--workspace-muted);
  font-size: 12px;
  text-align: center;
}

.icon-button {
  color: var(--workspace-muted);
}

.brand-mark {
  color: #ffffff;
  font-weight: 780;
}

.nav-item {
  color: var(--workspace-muted);
  font-size: 13px;
}

.nav-item.active {
  color: #ffffff;
  font-weight: 600;
}

.ghost-action {
  color: var(--workspace-muted);
}

.run-button,
.submit-button {
  height: 28px;
  border: 0;
  border-radius: 6px;
  font-weight: 600;
}

.run-button {
  color: #dbeafe;
  background: #1e293b;
}

.submit-button {
  color: #052e16;
  background: var(--workspace-accent);
}

.workspace {
  height: calc(100vh - 46px);
  padding: 10px;
  display: grid;
  grid-template-columns: minmax(360px, 29%) minmax(0, 1fr);
  gap: 10px;
}

.problem-pane,
.code-panel,
.bottom-panel {
  min-width: 0;
  border-radius: 8px;
  border: 1px solid var(--workspace-border);
  background: var(--workspace-surface);
  overflow: hidden;
}

.problem-pane {
  display: flex;
  flex-direction: column;
}

.pane-tabs {
  height: 40px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  border-bottom: 1px solid var(--workspace-border);
  background: var(--workspace-surface-2);
}

.pane-tabs button {
  height: 28px;
  padding: 0 10px;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  border: 0;
  border-radius: 6px;
  color: var(--workspace-muted);
  background: transparent;
  cursor: pointer;
}

.pane-tabs button.active {
  color: #ffffff;
  background: var(--workspace-surface);
}

.pane-tabs.compact {
  height: 38px;
}

.problem-scroll {
  padding: 22px 18px 28px;
  flex: 1;
  overflow: auto;
}

.problem-scroll h1 {
  margin: 0 0 14px;
  font-size: 24px;
  line-height: 1.3;
}

.meta-row {
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 22px;
}

.difficulty,
.tag,
.pass-rate {
  height: 24px;
  padding: 0 9px;
  display: inline-flex;
  align-items: center;
  border-radius: 12px;
  color: #dbeafe;
  background: #1e293b;
  font-size: 12px;
}

.difficulty.easy {
  color: #22c55e;
  background: rgba(34, 197, 94, 0.14);
}

.difficulty.medium {
  color: #f59e0b;
  background: rgba(245, 158, 11, 0.14);
}

.difficulty.hard {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.14);
}

.description,
.return-note {
  color: #e5edf7;
  font-size: 15px;
  line-height: 1.8;
}

.description-blocks {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.description-blocks p {
  margin: 0;
}

.description-table {
  margin: 2px 0 4px;
  padding: 12px;
  border: 1px solid #26364f;
  border-radius: 6px;
  color: #dbeafe;
  background: #0b1220;
  white-space: pre;
  font-size: 12px;
  line-height: 1.55;
}

.problem-html :deep(p) {
  margin: 0 0 14px;
}

.problem-html :deep(pre) {
  margin: 12px 0 18px;
  white-space: pre;
}

.problem-html :deep(code) {
  padding: 2px 5px;
  border-radius: 4px;
  color: #f9fafb;
  background: #243047;
}

.schema-section,
.example-section {
  margin-top: 26px;
}

.schema-section h2,
.example-section h2 {
  margin: 0 0 12px;
  color: #ffffff;
  font-size: 16px;
}

.schema-table {
  margin-bottom: 18px;
}

.schema-name,
.case-title {
  margin-bottom: 8px;
  color: #cbd5e1;
  font-size: 13px;
}

table {
  width: 100%;
  border-collapse: collapse;
  color: #f1f5f9;
  font-size: 13px;
}

th,
td {
  padding: 9px 10px;
  border: 1px solid var(--workspace-border);
  text-align: left;
}

th {
  color: #cbd5e1;
  background: var(--workspace-surface-2);
}

pre {
  margin: 0;
  padding: 14px;
  border-radius: 6px;
  color: #f4f4f5;
  background: #0f172a;
  overflow: auto;
  font-family: Consolas, "Courier New", monospace;
  font-size: 13px;
  line-height: 1.7;
}

.empty-state {
  min-height: 220px;
  display: grid;
  place-items: center;
  gap: 10px;
  color: var(--workspace-muted);
  text-align: center;
}

.empty-state .el-icon {
  font-size: 28px;
}

.tab-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ai-history-item {
  padding: 12px 14px;
  border: 1px solid var(--workspace-border);
  border-radius: 8px;
  background: #0f172a;
}

.ai-history-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--workspace-muted);
  margin-bottom: 6px;
}

.ai-history-text {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
  color: #e6e6e6;
  white-space: pre-wrap;
}

.submission-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 14px;
  border: 1px solid var(--workspace-border);
  border-radius: 8px;
  background: #0f172a;
  font-size: 13px;
  color: #cfcfcf;
}

.submission-time {
  margin-left: auto;
  color: var(--workspace-muted);
}

.coding-pane {
  min-width: 0;
  display: grid;
  grid-template-rows: minmax(320px, 1fr) 310px;
  gap: 8px;
}

.code-panel {
  display: grid;
  grid-template-rows: 42px minmax(0, 1fr);
}

.panel-titlebar {
  padding: 0 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--workspace-border);
  background: var(--workspace-surface-2);
}

.title-left,
.title-tools {
  gap: 10px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--workspace-accent);
  box-shadow: 0 0 0 4px rgba(34, 197, 94, 0.12);
}

.language-select {
  width: 96px;
}

.smart-mode {
  color: var(--workspace-muted);
  font-size: 13px;
}

.bottom-panel {
  display: flex;
  flex-direction: column;
}

.case-body,
.result-body {
  padding: 14px 18px;
  flex: 1;
  overflow: auto;
}

.case-pill {
  width: fit-content;
  margin-bottom: 14px;
  padding: 7px 16px;
  border-radius: 8px;
  color: #ffffff;
  background: #263449;
  font-weight: 700;
  font-size: 13px;
}

.case-block {
  margin-bottom: 18px;
}

.result-body {
  border-top: 2px solid transparent;
}

.result-body.accepted {
  border-color: #22c55e;
}

.result-body.failed {
  border-color: #ef4444;
}

.result-body.pending {
  border-color: #409eff;
}

.result-head {
  gap: 14px;
  margin-bottom: 12px;
}

.result-message {
  margin: 0 0 14px;
  color: #cbd5e1;
}

.ai-button {
  width: 180px;
  margin-bottom: 14px;
}

.run-result-table {
  --el-table-bg-color: var(--workspace-surface);
  --el-table-tr-bg-color: #111827;
  --el-table-header-bg-color: #162033;
  --el-table-text-color: #f0f0f0;
  --el-table-header-text-color: #d0d0d0;
}

:deep(.monaco-host) {
  height: 100%;
  border: 0;
  border-radius: 0;
}

:deep(.el-select__wrapper) {
  min-height: 28px;
  background: #111827;
  box-shadow: 0 0 0 1px var(--workspace-border) inset;
}

@media (max-width: 920px) {
  .leetcode-sql {
    height: auto;
    min-height: 100vh;
    overflow: auto;
  }

  .topbar {
    height: auto;
    min-height: 46px;
    flex-wrap: wrap;
    gap: 8px;
    padding: 8px 10px;
  }

  .topbar-left,
  .topbar-actions {
    flex-wrap: wrap;
  }

  .workspace {
    height: auto;
    grid-template-columns: 1fr;
  }

  .problem-pane {
    min-height: 520px;
  }

  .coding-pane {
    grid-template-rows: 560px 320px;
  }
}
</style>
