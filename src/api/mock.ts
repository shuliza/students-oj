import type { ActivityItem, ClassGroup, Problem, Submission, User } from '@/types'
import { leetcodeProblems } from './leetcode-mock'

export const currentStudent: User = {
  id: 1,
  username: 'student01',
  realName: '林同学',
  role: 'STUDENT',
  studentNo: '20260001',
  email: 'student01@example.edu',
  groupName: '数据库 1 班',
  status: 'ACTIVE'
}

export const currentTeacher: User = {
  id: 9,
  username: 'teacher01',
  realName: '王老师',
  role: 'TEACHER',
  email: 'teacher01@example.edu',
  status: 'ACTIVE'
}

export const problems: Problem[] = [
  {
    id: 101,
    title: '查询高分学生名单',
    difficulty: 'EASY',
    tags: ['SELECT', 'WHERE'],
    status: 'ACCEPTED',
    passRate: 82,
    submissions: 368,
    description: '从 student 与 score 表中查询成绩不低于 80 分的学生姓名、课程名和成绩。',
    sampleInput: 'student(id, name), score(student_id, course, score)',
    sampleOutput: 'name | course | score'
  },
  {
    id: 102,
    title: '统计每门课程平均分',
    difficulty: 'MEDIUM',
    tags: ['GROUP BY', 'AVG'],
    status: 'FAILED',
    passRate: 61,
    submissions: 280,
    description: '按课程统计平均分，并按照平均分从高到低排序。',
    sampleInput: 'score(course, score)',
    sampleOutput: 'course | avg_score'
  },
  {
    id: 103,
    title: '查询没有选课的学生',
    difficulty: 'MEDIUM',
    tags: ['LEFT JOIN', 'NULL'],
    status: 'TODO',
    passRate: 45,
    submissions: 192,
    description: '找出 student 表中没有任何选课记录的学生。',
    sampleInput: 'student(id, name), enroll(student_id, course_id)',
    sampleOutput: 'id | name'
  },
  {
    id: 104,
    title: '窗口函数排名',
    difficulty: 'HARD',
    tags: ['WINDOW', 'RANK'],
    status: 'TODO',
    passRate: 34,
    submissions: 126,
    description: '使用窗口函数计算每门课程内的成绩排名。',
    sampleInput: 'score(student_id, course, score)',
    sampleOutput: 'student_id | course | score | rank_no'
  },
  ...leetcodeProblems
]

export const submissions: Submission[] = [
  { id: 1024, problemId: 101, problemTitle: '查询高分学生名单', userId: 1, userName: '林同学', status: 'ACCEPTED', score: 100, runtimeMs: 32, submittedAt: '2026-05-26 10:02' },
  { id: 1025, problemId: 102, problemTitle: '统计每门课程平均分', userId: 1, userName: '林同学', status: 'WA', score: 40, runtimeMs: 41, submittedAt: '2026-05-26 10:18' },
  { id: 1026, problemId: 103, problemTitle: '查询没有选课的学生', userId: 2, userName: '陈同学', status: 'TLE', score: 0, runtimeMs: 3000, submittedAt: '2026-05-26 10:21' },
  { id: 1027, problemId: 104, problemTitle: '窗口函数排名', userId: 3, userName: '周同学', status: 'RE', score: 0, runtimeMs: 59, submittedAt: '2026-05-26 10:35' }
]

export const students: User[] = Array.from({ length: 12 }, (_, index) => ({
  id: index + 1,
  username: `student${String(index + 1).padStart(2, '0')}`,
  realName: `${['林', '陈', '周', '李', '赵', '吴'][index % 6]}同学`,
  role: 'STUDENT',
  studentNo: `202600${String(index + 1).padStart(2, '0')}`,
  groupName: index % 2 === 0 ? '数据库 1 班' : '数据库 2 班',
  status: index === 8 ? 'DISABLED' : 'ACTIVE'
}))

export const groups: ClassGroup[] = [
  { id: 1, name: '数据库 1 班', teacherName: '王老师', studentCount: 42, description: '周一 1-2 节实验班' },
  { id: 2, name: '数据库 2 班', teacherName: '王老师', studentCount: 39, description: '周三 3-4 节实验班' },
  { id: 3, name: '期末强化组', teacherName: '王老师', studentCount: 18, description: '低通过率题目专项练习' }
]

export const activity: ActivityItem[] = Array.from({ length: 84 }, (_, index) => {
  const date = new Date('2026-03-04T00:00:00')
  date.setDate(date.getDate() + index)
  return {
    date: date.toISOString().slice(0, 10),
    count: (index * 7 + index % 5) % 6
  }
})
