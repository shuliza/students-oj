# LeetCode CN 数据库题目爬虫

## 功能说明

本脚本用于从 leetcode.cn 网站爬取数据库相关的题目，用于 student-oj 项目的题库建设。

## 使用方法

### 1. 安装依赖

```bash
npm install
```

### 2. 运行爬虫

```bash
npm run crawl
```

### 3. 输出文件

爬取的数据将保存到 `data/leetcode-database-problems.json` 文件中。

## 文件说明

- `scripts/leetcode-api.ts` - LeetCode CN GraphQL API 封装
- `scripts/leetcode-crawler.ts` - 爬虫主程序
- `data/leetcode-database-problems.json` - 爬取结果（运行后生成）

## 数据结构

每个题目包含以下字段：

```typescript
{
  id: number           // 题目ID
  title: string        // 题目标题
  titleSlug: string    // URL slug
  difficulty: string   // 难度: EASY/MEDIUM/HARD
  tags: string[]       // 标签列表
  passRate: number     // 通过率
  submissions: number  // 提交次数
  description: string  // 题目描述
  sampleInput: string  // 示例输入
  sampleOutput: string // 示例输出
  hints: string[]      // 提示
}
```

## 注意事项

1. 爬虫会自动控制请求频率（1-3秒间隔），避免触发反爬机制
2. 请遵守 LeetCode 的使用条款，仅用于学习目的
3. 如果遇到网络错误，爬虫会自动跳过当前题目继续处理