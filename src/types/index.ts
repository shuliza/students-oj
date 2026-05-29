export type UserRole = 'STUDENT' | 'TEACHER' | 'ADMIN'

export interface User {
  id: number
  username: string
  realName: string
  role: UserRole
  studentNo?: string
  email?: string
  groupName?: string
  status: 'ACTIVE' | 'DISABLED'
}

export interface Problem {
  id: number
  title: string
  difficulty: 'EASY' | 'MEDIUM' | 'HARD'
  tags: string[]
  status: 'TODO' | 'ACCEPTED' | 'FAILED'
  passRate: number
  submissions: number
  description: string
  sampleInput: string
  sampleOutput: string
}

export interface Submission {
  id: number
  problemId: number
  problemTitle: string
  userId: number
  userName: string
  status: 'ACCEPTED' | 'WRONG_ANSWER' | 'TIME_LIMIT' | 'RUNTIME_ERROR' | 'WA' | 'TLE' | 'RE' | 'PENDING'
  score: number
  runtimeMs: number
  submittedAt: string
  message?: string
}

export interface ActivityItem {
  date: string
  count: number
}

export interface ClassGroup {
  id: number
  name: string
  teacherName: string
  studentCount: number
  description: string
}

export interface StudentTodaySolved {
  userId: number
  studentNo: string
  realName: string
  groupName: string
  todaySolved: number
}
