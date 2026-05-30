<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import { ArrowLeft, Document, MagicStick, Operation, RefreshRight, Upload, VideoPlay } from '@element-plus/icons-vue'
import SqlMonacoEditor from '@/components/SqlMonacoEditor.vue'
import StatusTag from '@/components/StatusTag.vue'
import { aiApi, problemApi, submissionApi } from '@/api'
import type { Problem, Submission } from '@/types'

type SchemaTable = {
  name: string
  columns: string[]
}

const route = useRoute()
const router = useRouter()
const problem = ref<Problem | null>(null)
const DEFAULT_SQL = 'SELECT s.name, c.course, c.score\nFROM student s\nJOIN score c ON s.id = c.student_id\nWHERE c.score >= 80;'
const sql = ref(DEFAULT_SQL)
const result = ref<Submission | null>(null)
const suggestion = ref('')
const judging = ref(false)
const running = ref(false)
const aiLoading = ref(false)
const activeProblemTab = ref('description')
const activeBottomTab = ref('cases')
const runResult = ref<{ status: string; runtimeMs: number; message: string; match: boolean } | null>(null)
const mySubmissions = ref<Submission[]>([])
const aiHistory = ref<Array<{ suggestion: string; createdAt?: string }>>([])

const schemaTables = computed(() => parseSchema(problem.value?.sampleInput ?? ''))
const statusTone = computed(() => {
  const status = result.value?.status
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

function parseSchema(input: string): SchemaTable[] {
  const matches = input.matchAll(/([\w\u4e00-\u9fa5]+)\(([^)]*)\)/g)
  return Array.from(matches, (match) => ({
    name: match[1],
    columns: match[2].split(',').map((item) => item.trim()).filter(Boolean)
  }))
}

onMounted(async () => {
  problem.value = await problemApi.detail(Number(route.params.id ?? 101))
  await Promise.all([loadMySubmissions(), loadAiHistory()])
})
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

            <p class="description">{{ problem.description }}</p>
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
            <div v-for="table in schemaTables" :key="table.name" class="case-block">
              <div class="case-title">{{ table.name }} =</div>
              <pre>| {{ table.columns.join(' | ') }} |
| {{ table.columns.map(() => '--------').join(' | ') }} |</pre>
            </div>
          </div>

          <div v-else class="result-body" :class="statusTone">
            <el-empty v-if="!result" description="暂无运行结果" />
            <template v-else>
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
  height: 100vh;
  color: #f5f5f5;
  background: #171717;
  overflow: hidden;
}

.topbar {
  height: 38px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #2f2f2f;
  background: #111111;
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

.icon-button {
  color: #bdbdbd;
}

.brand-mark {
  color: #e7e7e7;
  font-weight: 700;
}

.nav-item {
  color: #a8a8a8;
  font-size: 13px;
}

.nav-item.active {
  color: #ffffff;
  font-weight: 600;
}

.ghost-action {
  color: #bdbdbd;
}

.run-button,
.submit-button {
  height: 28px;
  border: 0;
  border-radius: 6px;
  font-weight: 600;
}

.run-button {
  color: #d7d7d7;
  background: #2c2c2c;
}

.submit-button {
  color: #19c37d;
  background: #18382c;
}

.workspace {
  height: calc(100vh - 38px);
  padding: 8px;
  display: grid;
  grid-template-columns: minmax(360px, 29%) minmax(0, 1fr);
  gap: 8px;
}

.problem-pane,
.code-panel,
.bottom-panel {
  min-width: 0;
  border-radius: 6px;
  border: 1px solid #303030;
  background: #242424;
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
  border-bottom: 1px solid #303030;
  background: #303030;
}

.pane-tabs button {
  height: 28px;
  padding: 0 10px;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  border: 0;
  border-radius: 6px;
  color: #a8a8a8;
  background: transparent;
  cursor: pointer;
}

.pane-tabs button.active {
  color: #ffffff;
  background: #242424;
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
  color: #d5d5d5;
  background: #373737;
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
  color: #e8e8e8;
  font-size: 15px;
  line-height: 1.8;
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
  color: #d8d8d8;
  font-size: 13px;
}

table {
  width: 100%;
  border-collapse: collapse;
  color: #f0f0f0;
  font-size: 13px;
}

th,
td {
  padding: 9px 10px;
  border: 1px solid #4a4a4a;
  text-align: left;
}

th {
  color: #bdbdbd;
  background: #303030;
}

pre {
  margin: 0;
  padding: 14px;
  border-radius: 6px;
  color: #f4f4f5;
  background: #3a3a3a;
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
  color: #a8a8a8;
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
  border: 1px solid #2f2f2f;
  border-radius: 8px;
  background: #1d1d1d;
}

.ai-history-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #8f8f8f;
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
  border: 1px solid #2f2f2f;
  border-radius: 8px;
  background: #1d1d1d;
  font-size: 13px;
  color: #cfcfcf;
}

.submission-time {
  margin-left: auto;
  color: #8f8f8f;
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
  border-bottom: 1px solid #303030;
  background: #303030;
}

.title-left,
.title-tools {
  gap: 10px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #22c55e;
}

.language-select {
  width: 96px;
}

.smart-mode {
  color: #8f8f8f;
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
  background: #4a4a4a;
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
  color: #b9c2d0;
}

.ai-button {
  width: 180px;
  margin-bottom: 14px;
}

:deep(.monaco-host) {
  height: 100%;
  border: 0;
  border-radius: 0;
}

:deep(.el-select__wrapper) {
  min-height: 28px;
  background: #2b2b2b;
  box-shadow: 0 0 0 1px #444444 inset;
}

@media (max-width: 920px) {
  .leetcode-sql {
    height: auto;
    min-height: 100vh;
    overflow: auto;
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
