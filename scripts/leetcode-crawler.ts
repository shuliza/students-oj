import { leetcodeApi, LeetCodeQuestion } from './leetcode-api'
import * as fs from 'fs'
import * as path from 'path'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

interface Problem {
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

function mapDifficulty(difficulty: 'Easy' | 'Medium' | 'Hard'): 'EASY' | 'MEDIUM' | 'HARD' {
  const mapping: Record<string, 'EASY' | 'MEDIUM' | 'HARD'> = {
    'Easy': 'EASY',
    'Medium': 'MEDIUM',
    'Hard': 'HARD'
  }
  return mapping[difficulty] || 'MEDIUM'
}

function sleep(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms))
}

function extractSampleFromContent(content: string): { input: string; output: string } {
  const inputMatch = content.match(/输入[：:]\s*([\s\S]*?)(?=输出[：:]|$)/i)
  const outputMatch = content.match(/输出[：:]\s*([\s\S]*?)(?=解释[：:]|示例|注意|$)/i)

  return {
    input: inputMatch ? inputMatch[1].trim().substring(0, 200) : '',
    output: outputMatch ? outputMatch[1].trim().substring(0, 200) : ''
  }
}

async function crawlLeetCodeProblems(): Promise<Problem[]> {
  const allProblems: Problem[] = []
  let skip = 0
  const limit = 50
  let total = 0

  console.log('Initializing LeetCode API...')
  await leetcodeApi.init()

  console.log('Starting to crawl LeetCode CN database problems...')

  do {
    try {
      console.log(`Fetching problems ${skip + 1} to ${skip + limit}...`)
      const result = await leetcodeApi.fetchProblemList(skip, limit)
      total = result.total

      for (const q of result.questions) {
        console.log(`Processing: ${q.frontendQuestionId}. ${q.titleCn}`)

        await sleep(1000 + Math.random() * 2000)

        try {
          const detail = await leetcodeApi.fetchQuestionDetail(q.titleSlug)
          const { input, output } = extractSampleFromContent(detail.content || '')

          allProblems.push({
            id: parseInt(q.frontendQuestionId),
            title: q.titleCn || q.title,
            titleSlug: q.titleSlug,
            difficulty: mapDifficulty(q.difficulty),
            tags: q.topicTags.map(t => t.nameTranslated || t.name),
            passRate: Math.round(q.acRate * 100) / 100,
            description: detail.content ? detail.content.replace(/<[^>]+>/g, '').substring(0, 500) : '',
            sampleInput: input || detail.sampleTestCase || '',
            sampleOutput: output || '',
            hints: detail.hints || []
          })
        } catch (err) {
          console.error(`Failed to fetch detail for ${q.titleSlug}, skipping...`)
          allProblems.push({
            id: parseInt(q.frontendQuestionId),
            title: q.titleCn || q.title,
            titleSlug: q.titleSlug,
            difficulty: mapDifficulty(q.difficulty),
            tags: q.topicTags.map(t => t.nameTranslated || t.name),
            passRate: Math.round(q.acRate * 100) / 100,
            description: '',
            sampleInput: '',
            sampleOutput: '',
            hints: []
          })
        }
      }

      skip += limit
      await sleep(2000)
    } catch (error) {
      console.error('Error fetching problem list:', error)
      break
    }
  } while (skip < total)

  return allProblems
}

async function saveProblems(problems: Problem[]): Promise<void> {
  const outputDir = path.join(__dirname, '..', 'data')
  if (!fs.existsSync(outputDir)) {
    fs.mkdirSync(outputDir, { recursive: true })
  }

  const outputPath = path.join(outputDir, 'leetcode-database-problems.json')
  fs.writeFileSync(outputPath, JSON.stringify(problems, null, 2), 'utf-8')
  console.log(`Saved ${problems.length} problems to ${outputPath}`)
}

async function main() {
  try {
    const problems = await crawlLeetCodeProblems()
    await saveProblems(problems)
    console.log('Crawling completed successfully!')
  } catch (error) {
    console.error('Crawling failed:', error)
    process.exit(1)
  }
}

main()
