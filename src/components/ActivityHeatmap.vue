<script setup lang="ts">
import { computed } from 'vue'
import type { ActivityItem } from '@/types'

const props = withDefaults(defineProps<{
  data: ActivityItem[]
  months?: number
}>(), {
  months: 3
})

interface DayCell {
  date: string
  day: number
  week: number
  weekday: number
  count: number
  isToday: boolean
}

interface MonthBlock {
  key: string
  label: string
  year: number
  month: number
  weeks: number
  days: DayCell[]
}

const MS_PER_DAY = 24 * 60 * 60 * 1000

const pad = (value: number) => String(value).padStart(2, '0')

const formatDate = (date: Date) => `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`

const parseDate = (value: string) => {
  const [year, month, day] = value.split('-').map(Number)
  return new Date(year, month - 1, day)
}

const formatDisplayDate = (date: string) => {
  const [year, month, day] = date.split('-')
  return `${year}-${month}-${day}`
}

const monthLabel = (year: number, month: number) => `${month + 1}月`

const activityMap = computed(() => {
  const map = new Map<string, number>()
  props.data.forEach((item) => {
    map.set(item.date, item.count)
  })
  return map
})

const todayKey = computed(() => formatDate(new Date()))

const monthBlocks = computed<MonthBlock[]>(() => {
  const now = new Date()
  const count = Math.max(1, props.months)

  return Array.from({ length: count }, (_, index) => {
    const date = new Date(now.getFullYear(), now.getMonth() - (count - 1 - index), 1)
    const year = date.getFullYear()
    const month = date.getMonth()
    const daysInMonth = new Date(year, month + 1, 0).getDate()
    const firstDay = new Date(year, month, 1).getDay()
    const weeks = Math.ceil((firstDay + daysInMonth) / 7)

    const days = Array.from({ length: daysInMonth }, (_, dayIndex) => {
      const day = dayIndex + 1
      const key = formatDate(new Date(year, month, day))
      const offset = firstDay + dayIndex
      return {
        date: key,
        day,
        week: Math.floor(offset / 7),
        weekday: offset % 7,
        count: activityMap.value.get(key) ?? 0,
        isToday: key === todayKey.value
      }
    })

    return {
      key: `${year}-${pad(month + 1)}`,
      label: monthLabel(year, month),
      year,
      month,
      weeks,
      days
    }
  })
})

const totalSubmissions = computed(() =>
  monthBlocks.value.reduce((sum, month) => sum + month.days.reduce((monthSum, day) => monthSum + day.count, 0), 0)
)

const activeDays = computed(() =>
  monthBlocks.value.reduce((sum, month) => sum + month.days.filter((day) => day.count > 0).length, 0)
)

const continuousDays = computed(() => {
  const countedDates = new Set<string>()
  props.data.forEach((item) => {
    if (item.count > 0) countedDates.add(item.date)
  })

  let streak = 0
  const cursor = parseDate(todayKey.value)
  while (countedDates.has(formatDate(cursor))) {
    streak += 1
    cursor.setTime(cursor.getTime() - MS_PER_DAY)
  }
  return streak
})

const summaryText = computed(() => {
  if (props.months === 1) return `本月共提交 ${totalSubmissions.value} 次`
  return `近 ${props.months} 个月共提交 ${totalSubmissions.value} 次`
})

const levelClass = (count: number) => {
  if (count <= 0) return 'level-0'
  if (count <= 2) return 'level-1'
  if (count <= 5) return 'level-2'
  if (count <= 10) return 'level-3'
  return 'level-4'
}

const tooltip = (day: DayCell) => `${day.count} 个提交, ${formatDisplayDate(day.date)}`
</script>

<template>
  <div class="activity-panel">
    <div class="activity-summary">
      <div class="summary-main">{{ summaryText }}</div>
      <div class="summary-meta">
        <span>累计提交天数: {{ activeDays }}</span>
        <span>连续提交: {{ continuousDays }}</span>
      </div>
    </div>

    <div class="months" :class="{ 'single-month': months === 1 }">
      <div v-for="month in monthBlocks" :key="month.key" class="month-block">
        <div class="month-grid" :style="{ '--weeks': month.weeks }">
          <button
            v-for="day in month.days"
            :key="day.date"
            class="day-cell"
            :class="[levelClass(day.count), { today: day.isToday }]"
            :style="{ gridColumn: day.week + 1, gridRow: day.weekday + 1 }"
            type="button"
            :aria-label="tooltip(day)"
          >
            <span class="tooltip">{{ tooltip(day) }}</span>
          </button>
        </div>
        <div class="month-label">{{ month.label }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.activity-panel {
  width: 100%;
}

.activity-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
  color: var(--color-text);
}

.summary-main {
  font-size: 16px;
  font-weight: 700;
}

.summary-meta {
  display: flex;
  gap: 16px;
  color: var(--color-muted);
  font-size: 13px;
  white-space: nowrap;
}

.months {
  display: flex;
  gap: 20px;
  overflow-x: auto;
  padding: 34px 2px 10px;
  margin-top: -32px;
}

.single-month {
  justify-content: center;
}

.month-block {
  flex: 0 0 auto;
}

.month-grid {
  --cell-size: 12px;
  --cell-gap: 4px;
  display: grid;
  grid-template-columns: repeat(var(--weeks), var(--cell-size));
  grid-template-rows: repeat(7, var(--cell-size));
  gap: var(--cell-gap);
  min-height: calc(var(--cell-size) * 7 + var(--cell-gap) * 6);
}

.day-cell {
  position: relative;
  width: var(--cell-size);
  height: var(--cell-size);
  padding: 0;
  border: 0;
  border-radius: 4px;
  background: #e2e8f0;
  cursor: default;
}

.day-cell:hover,
.day-cell:focus-visible {
  outline: 2px solid rgba(30, 64, 175, 0.35);
  outline-offset: 1px;
}

.day-cell.today {
  box-shadow: 0 0 0 1px rgba(30, 64, 175, 0.75);
}

.level-0 {
  background: #e2e8f0;
}

.level-1 {
  background: #bbf7d0;
}

.level-2 {
  background: #4ade80;
}

.level-3 {
  background: #22c55e;
}

.level-4 {
  background: #15803d;
}

.tooltip {
  position: absolute;
  left: 50%;
  bottom: calc(100% + 8px);
  z-index: 4;
  width: max-content;
  max-width: 180px;
  padding: 6px 10px;
  border-radius: 8px;
  background: rgba(31, 41, 55, 0.95);
  color: #ffffff;
  font-size: 12px;
  line-height: 1.4;
  pointer-events: none;
  opacity: 0;
  transform: translateX(-50%) translateY(4px);
  transition: opacity 0.15s ease, transform 0.15s ease;
  white-space: nowrap;
}

.tooltip::after {
  content: "";
  position: absolute;
  left: 50%;
  top: 100%;
  border: 5px solid transparent;
  border-top-color: rgba(31, 41, 55, 0.95);
  transform: translateX(-50%);
}

.day-cell:hover .tooltip,
.day-cell:focus-visible .tooltip {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
}

.month-label {
  margin-top: 8px;
  color: var(--color-muted);
  font-size: 13px;
  text-align: center;
}

@media (max-width: 640px) {
  .activity-summary {
    align-items: flex-start;
    flex-direction: column;
  }

  .summary-meta {
    flex-wrap: wrap;
    gap: 8px 14px;
    white-space: normal;
  }

  .months {
    gap: 18px;
  }
}
</style>
