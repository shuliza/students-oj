# MySQL 初始化脚本

MySQL 容器首次启动时，会按文件名顺序执行挂载到 `/docker-entrypoint-initdb.d/` 下的 `.sql` 文件。脚本只会在数据卷为空时执行。

| 执行顺序 | 容器内文件名 | 源文件 | 作用 |
| --- | --- | --- | --- |
| 1 | `01-init.sql` | `init.sql` | 建库建表、沙箱库与账号、班级/用户/示例题等基础种子 |
| 2 | `02-problems.sql` | `02-problems.sql` | 题库数据与测试用例 |

`02-problems.sql` 会先清理 `problem` 与 `problem_testcase`，再导入完整题库数据。

重新初始化：

```bash
docker compose down -v
docker compose up -d --build
```

## 重新生成 02-problems.sql

如果运行中的数据库里更新了题目，可以用 `mysqldump` 重新导出：

```bash
{
  cat <<'HEADER'
-- ---------------------------------------------------------------------------
-- Student OJ 题库种子：SQL 题目 + 测试用例
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
