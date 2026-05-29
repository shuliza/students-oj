import axios, { AxiosInstance } from 'axios'

const LEETCODE_CN_GRAPHQL = 'https://leetcode.cn/graphql/'
const LEETCODE_CN_PROBLEMSET = 'https://leetcode.cn/problemset/all/'

interface LeetCodeQuestion {
  frontendQuestionId: string
  titleCn: string
  title: string
  difficulty: 'Easy' | 'Medium' | 'Hard'
  topicTags: Array<{
    name: string
    nameTranslated: string
  }>
  status: string | null
  acRate: number
  titleSlug: string
}

interface LeetCodeQuestionDetail {
  questionId: string
  title: string
  titleCn: string
  content: string
  difficulty: string
  topicTags: Array<{
    name: string
    nameTranslated: string
  }>
  hints: string[]
  sampleTestCase: string
}

class LeetCodeAPI {
  private client: AxiosInstance
  private csrfToken: string = ''

  constructor() {
    this.client = axios.create({
      headers: {
        'Content-Type': 'application/json',
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'Referer': 'https://leetcode.cn/',
        'Origin': 'https://leetcode.cn'
      },
      withCredentials: true
    })
  }

  async init(): Promise<void> {
    try {
      const response = await axios.get(LEETCODE_CN_PROBLEMSET, {
        headers: {
          'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
        }
      })

      const cookies = response.headers['set-cookie']
      if (cookies) {
        const csrfCookie = cookies.find((c: string) => c.includes('csrftoken'))
        if (csrfCookie) {
          const match = csrfCookie.match(/csrftoken=([^;]+)/)
          if (match) {
            this.csrfToken = match[1]
            this.client.defaults.headers.common['X-CSRFToken'] = this.csrfToken
            this.client.defaults.headers.common['Cookie'] = `csrftoken=${this.csrfToken}`
          }
        }
      }

      console.log('CSRF Token obtained:', this.csrfToken ? 'Yes' : 'No')
    } catch (error) {
      console.warn('Failed to get CSRF token, proceeding without it:', error)
    }
  }

  async fetchProblemList(skip: number = 0, limit: number = 50): Promise<{
    total: number
    questions: LeetCodeQuestion[]
  }> {
    const query = `
      query problemsetQuestionList($categorySlug: String, $limit: Int, $skip: Int, $filters: QuestionListFilterInput) {
        problemsetQuestionList(categorySlug: $categorySlug, limit: $limit, skip: $skip, filters: $filters) {
          total
          questions {
            frontendQuestionId
            titleCn
            title
            difficulty
            topicTags {
              name
              nameTranslated
            }
            status
            acRate
            titleSlug
          }
        }
      }
    `

    const variables = {
      categorySlug: '',
      skip,
      limit,
      filters: {
        tags: ['database']
      }
    }

    try {
      const response = await this.client.post(LEETCODE_CN_GRAPHQL, {
        query,
        variables
      })

      if (response.data.errors) {
        console.error('GraphQL errors:', response.data.errors)
        throw new Error(response.data.errors[0].message)
      }

      return response.data.data.problemsetQuestionList
    } catch (error: any) {
      if (error.response?.status === 400) {
        console.error('Bad request - Response:', error.response?.data)
      }
      throw error
    }
  }

  async fetchQuestionDetail(titleSlug: string): Promise<LeetCodeQuestionDetail> {
    const query = `
      query questionDetail($titleSlug: String!) {
        question(titleSlug: $titleSlug) {
          questionId
          title
          titleCn
          content
          difficulty
          topicTags {
            name
            nameTranslated
          }
          hints
          sampleTestCase
        }
      }
    `

    const variables = { titleSlug }

    try {
      const response = await this.client.post(LEETCODE_CN_GRAPHQL, {
        query,
        variables
      })

      if (response.data.errors) {
        throw new Error(response.data.errors[0].message)
      }

      return response.data.data.question
    } catch (error) {
      throw error
    }
  }
}

export type { LeetCodeQuestion, LeetCodeQuestionDetail }
export const leetcodeApi = new LeetCodeAPI()
