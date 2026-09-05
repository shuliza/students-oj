import type { ActivityItem } from '@/types'

const pad = (value: number) => String(value).padStart(2, '0')

const localDateKey = (date: Date) =>
  `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`

export function recentActivity(data: ActivityItem[], days: number): ActivityItem[] {
  const counts = new Map<string, number>()
  data.forEach((item) => counts.set(item.date, (counts.get(item.date) ?? 0) + item.count))

  const today = new Date()
  return Array.from({ length: days }, (_, index) => {
    const date = new Date(today.getFullYear(), today.getMonth(), today.getDate() - (days - 1 - index))
    const key = localDateKey(date)
    return { date: key, count: counts.get(key) ?? 0 }
  })
}
