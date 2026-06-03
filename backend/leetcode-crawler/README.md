# LeetCode SQL Crawler

独立 Spring Boot 模块，用于采集 LeetCode Database 分类公开 SQL 题目并写入 `sql_problem`。

## 功能

- GraphQL 获取 Database 题目列表和题目详情
- 过滤非 SQL/Database 与付费题
- 保留 HTML 题面并生成纯文本
- 提取示例、表结构、样例数据、样例输出
- 基于样例生成 `test_cases` JSON
- `title_slug` 唯一键增量 upsert
- Spring Retry 三次指数退避
- 每次请求随机等待 1000-2000ms
- 每天 02:00 自动同步

## 本地运行

```bash
cd backend
mvn -pl leetcode-crawler -am spring-boot:run
```

默认连接：

```yaml
MYSQL_HOST: localhost
MYSQL_PORT: 3306
MYSQL_DATABASE: student_oj
MYSQL_USERNAME: root
MYSQL_PASSWORD: root
```

如果 LeetCode 对匿名访问限流，可配置浏览器登录后的 Cookie：

```bash
set LEETCODE_COOKIE=LEETCODE_SESSION=...; csrftoken=...
```

## 手动同步

```bash
curl -X POST http://localhost:8088/crawler/leetcode/sync/full
curl -X POST http://localhost:8088/crawler/leetcode/sync/incremental
```

## Docker

根目录执行：

```bash
docker compose up -d mysql leetcode-crawler
```

通用后端 Dockerfile 会通过 `SERVICE_NAME=leetcode-crawler` 构建本模块。
