import * as fs from 'fs'
import * as path from 'path'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

interface CrawledProblem {
  id: number
  title: string
  titleSlug: string
  difficulty: 'EASY' | 'MEDIUM' | 'HARD'
  tags: string[]
  passRate: number
  description: string
  sampleInput: string
  sampleOutput: string
  hints: string[]
}

interface MockProblem {
  id: number
  title: string
  difficulty: 'EASY' | 'MEDIUM' | 'HARD'
  tags: string[]
  status: 'TODO'
  passRate: number
  submissions: number
  description: string
  sampleInput: string
  sampleOutput: string
}

function convertToMockFormat(problems: CrawledProblem[]): MockProblem[] {
  return problems.map(p => ({
    id: p.id + 1000,
    title: p.title,
    difficulty: p.difficulty,
    tags: p.tags.slice(0, 3),
    status: 'TODO' as const,
    passRate: p.passRate,
    submissions: 0,
    description: p.description.substring(0, 200) || `来自 LeetCode 的数据库题目: ${p.title}`,
    sampleInput: p.sampleInput || '请参考题目描述',
    sampleOutput: p.sampleOutput || '请参考题目描述'
  }))
}

function main() {
  const inputPath = path.join(__dirname, '..', 'data', 'leetcode-database-problems.json')
  const outputPath = path.join(__dirname, '..', 'src', 'api', 'leetcode-mock.ts')

  if (!fs.existsSync(inputPath)) {
    console.error('Input file not found. Please run the crawler first: npm run crawl')
    process.exit(1)
  }

  const crawled: CrawledProblem[] = JSON.parse(fs.readFileSync(inputPath, 'utf-8'))
  const mockProblems = convertToMockFormat(crawled)

  const content = `import type { Problem } from '@/types'

export const leetcodeProblems: Problem[] = ${JSON.stringify(mockProblems, null, 2)}
`

  fs.writeFileSync(outputPath, content, 'utf-8')
  console.log(`Converted ${mockProblems.length} problems to mock format`)
  console.log(`Output saved to: ${outputPath}`)
}

main()
