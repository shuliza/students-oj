# MySQL 初始化脚本

容器首次启动时，MySQL 镜像会按**文件名字母序**执行挂载到
`/docker-entrypoint-initdb.d/` 下的 `.sql` 文件（仅在数据卷为空时）。
两个 compose 文件都把本目录的脚本按下列顺序挂载：

| 执行序 | 容器内文件名 | 源文件 | 作用 |
|---|---|---|---|
| 1 | `01-init.sql` | `init.sql` | 建库建表、沙箱库与账号、班级/用户/示例题等基础种子 |
| 2 | `02-problems.sql` | `02-problems.sql` | 题库：323 道 SQL 题目 + 对应测试用例（id 1-323） |

`02-problems.sql` 开头会 `DELETE FROM problem / problem_testcase` 再灌入，
覆盖 `init.sql` 里的示例题，保证全新部署即为完整 323 题题库；脚本可重复执行。

> 注意：initdb 脚本**只在数据卷为空（首次启动）时执行**。已有数据的卷不会重跑。
> 要重新初始化，需先删卷：`docker compose -f docker-compose.monolith.yml down -v`。

## 重新生成 02-problems.sql

题库数据以本 SQL 文件为准纳入版本管理。若在运行中的库里更新了题目
（如爬虫新增、手工编辑），用 `mysqldump` 重新导出覆盖本文件：

```bash
# 容器名按实际调整（单体栈为 student-oj-mysql-1）
{
  cat <<'HEADER'
-- ---------------------------------------------------------------------------
-- Student OJ 题库种子：SQL 题目 + 对应测试用例
-- 数据来源：LeetCode 题库爬取整理。容器首启时按文件名序在 init.sql 之后执行。
-- 重新生成方式见 deploy/mysql/README.md。
-- ---------------------------------------------------------------------------
USE student_oj;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM problem_testcase;
DELETE FROM problem;

HEADER
  docker exec student-oj-mysql-1 mysqldump -uroot -proot \
    --default-character-set=utf8mb4 \
    --no-create-info --complete-insert --skip-extended-insert \
    --skip-comments --single-transaction \
    student_oj problem problem_testcase
  printf '\nSET FOREIGN_KEY_CHECKS = 1;\n'
} > deploy/mysql/02-problems.sql
```

导出后建议在全新临时容器里验证可干净执行（挂载 init.sql + 02-problems.sql，
检查 `SELECT COUNT(*) FROM problem` 是否符合预期）再提交。
