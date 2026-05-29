-- ---------------------------------------------------------------------------
-- Student OJ 初始化脚本：业务库 student_oj + 沙箱库 student_oj_sandbox
-- 容器首次启动时由 MySQL 镜像自动执行（挂在 /docker-entrypoint-initdb.d/）。
-- ---------------------------------------------------------------------------

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS student_oj         DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS student_oj_sandbox DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- =================== 业务库 ===================
USE student_oj;

CREATE TABLE IF NOT EXISTS class_group (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  name          VARCHAR(64)  NOT NULL,
  teacher_name  VARCHAR(64)  NOT NULL DEFAULT '',
  description   VARCHAR(255) NOT NULL DEFAULT '',
  student_count INT          NOT NULL DEFAULT 0,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_class_group_name (name)
);

CREATE TABLE IF NOT EXISTS user (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  username      VARCHAR(64)  NOT NULL,
  password_hash VARCHAR(255) NOT NULL DEFAULT '',
  real_name     VARCHAR(64)  NOT NULL DEFAULT '',
  email         VARCHAR(128) NOT NULL DEFAULT '',
  role          VARCHAR(16)  NOT NULL DEFAULT 'STUDENT',
  student_no    VARCHAR(32)  NOT NULL DEFAULT '',
  group_id      BIGINT       DEFAULT NULL,
  status        VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_username (username),
  KEY idx_user_group (group_id)
);

CREATE TABLE IF NOT EXISTS problem (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  title         VARCHAR(128) NOT NULL,
  description   TEXT,
  difficulty    VARCHAR(16)  NOT NULL DEFAULT 'EASY',
  tags          VARCHAR(255) NOT NULL DEFAULT '',
  init_sql      TEXT,
  answer_sql    TEXT,
  sample_input  TEXT,
  sample_output TEXT,
  status        INT          NOT NULL DEFAULT 1,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS submission (
  id           BIGINT       NOT NULL AUTO_INCREMENT,
  user_id      BIGINT       NOT NULL,
  problem_id   BIGINT       NOT NULL,
  sql_content  TEXT,
  status       VARCHAR(32)  NOT NULL DEFAULT 'PENDING',
  score        INT          NOT NULL DEFAULT 0,
  runtime_ms   INT          NOT NULL DEFAULT 0,
  message      TEXT,
  submitted_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_submission_user (user_id),
  KEY idx_submission_problem (problem_id),
  KEY idx_submission_time (submitted_at)
);

CREATE TABLE IF NOT EXISTS student_activity (
  id               BIGINT     NOT NULL AUTO_INCREMENT,
  user_id          BIGINT     NOT NULL,
  activity_date    DATE       NOT NULL,
  submission_count INT        NOT NULL DEFAULT 0,
  accepted_count   INT        NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_date (user_id, activity_date)
);

CREATE TABLE IF NOT EXISTS ai_suggestion (
  id            BIGINT   NOT NULL AUTO_INCREMENT,
  user_id       BIGINT   NOT NULL,
  submission_id BIGINT   NOT NULL,
  problem_id    BIGINT   NOT NULL,
  suggestion    TEXT,
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_ai_user_problem_time (user_id, problem_id, created_at),
  KEY idx_ai_submission (submission_id)
);

-- 班级
INSERT INTO class_group(id, name, teacher_name, description, student_count) VALUES
  (1, '数据库 1 班', '王老师', '周一 1-2 节实验班',     6),
  (2, '数据库 2 班', '王老师', '周三 3-4 节实验班',     6),
  (3, '期末强化组', '王老师', '低通过率题目专项练习', 0)
ON DUPLICATE KEY UPDATE student_count = VALUES(student_count);

-- 用户：password_hash 留空，由 auth-service 启动时用 BCrypt(123456) 回填
INSERT INTO user(id, username, password_hash, real_name, email, role, student_no, group_id, status) VALUES
  (1, 'teacher01', '', '王老师', 'teacher01@example.edu', 'TEACHER', '',         NULL, 'ACTIVE')
ON DUPLICATE KEY UPDATE real_name = VALUES(real_name);

INSERT INTO user(id, username, password_hash, real_name, email, role, student_no, group_id, status) VALUES
  (2,  'student01', '', '林同学', 'student01@example.edu', 'STUDENT', '20260001', 1, 'ACTIVE'),
  (3,  'student02', '', '陈同学', 'student02@example.edu', 'STUDENT', '20260002', 1, 'ACTIVE'),
  (4,  'student03', '', '周同学', 'student03@example.edu', 'STUDENT', '20260003', 1, 'ACTIVE'),
  (5,  'student04', '', '李同学', 'student04@example.edu', 'STUDENT', '20260004', 1, 'ACTIVE'),
  (6,  'student05', '', '赵同学', 'student05@example.edu', 'STUDENT', '20260005', 1, 'ACTIVE'),
  (7,  'student06', '', '吴同学', 'student06@example.edu', 'STUDENT', '20260006', 1, 'ACTIVE'),
  (8,  'student07', '', '林同学', 'student07@example.edu', 'STUDENT', '20260007', 2, 'ACTIVE'),
  (9,  'student08', '', '陈同学', 'student08@example.edu', 'STUDENT', '20260008', 2, 'ACTIVE'),
  (10, 'student09', '', '周同学', 'student09@example.edu', 'STUDENT', '20260009', 2, 'DISABLED'),
  (11, 'student10', '', '李同学', 'student10@example.edu', 'STUDENT', '20260010', 2, 'ACTIVE'),
  (12, 'student11', '', '赵同学', 'student11@example.edu', 'STUDENT', '20260011', 2, 'ACTIVE'),
  (13, 'student12', '', '吴同学', 'student12@example.edu', 'STUDENT', '20260012', 2, 'ACTIVE')
ON DUPLICATE KEY UPDATE real_name = VALUES(real_name);

-- 题目种子
INSERT INTO problem(id, title, description, difficulty, tags, init_sql, answer_sql, sample_input, sample_output, status) VALUES
  (101, '查询高分学生名单',
   '从 student 与 score 表中查询成绩不低于 80 分的学生姓名、课程名和成绩，按成绩从高到低排序。',
   'EASY', 'SELECT,WHERE',
   'CREATE TABLE student(id INT PRIMARY KEY, name VARCHAR(32)); CREATE TABLE score(student_id INT, course VARCHAR(32), score INT); INSERT INTO student VALUES (1,''林同学''),(2,''陈同学''),(3,''周同学''); INSERT INTO score VALUES (1,''数据库'',92),(2,''数据库'',75),(3,''数据库'',88);',
   'SELECT s.name, c.course, c.score FROM student s JOIN score c ON s.id = c.student_id WHERE c.score >= 80 ORDER BY c.score DESC;',
   'student(id, name), score(student_id, course, score)',
   'name | course | score', 1),
  (102, '统计每门课程平均分',
   '按课程统计平均分，结果列含 course 和 avg_score，按平均分从高到低排序。',
   'MEDIUM', 'GROUP BY,AVG',
   'CREATE TABLE score(student_id INT, course VARCHAR(32), score INT); INSERT INTO score VALUES (1,''数据库'',92),(2,''数据库'',75),(3,''数据库'',88),(1,''操作系统'',85),(2,''操作系统'',70);',
   'SELECT course, AVG(score) AS avg_score FROM score GROUP BY course ORDER BY avg_score DESC;',
   'score(course, score)',
   'course | avg_score', 1),
  (103, '查询没有选课的学生',
   '找出 student 表中没有任何选课记录的学生，返回 id 与 name。',
   'MEDIUM', 'LEFT JOIN,NULL',
   'CREATE TABLE student(id INT PRIMARY KEY, name VARCHAR(32)); CREATE TABLE enroll(student_id INT, course_id INT); INSERT INTO student VALUES (1,''林同学''),(2,''陈同学''),(3,''周同学''); INSERT INTO enroll VALUES (1,101),(2,102);',
   'SELECT s.id, s.name FROM student s LEFT JOIN enroll e ON s.id = e.student_id WHERE e.student_id IS NULL;',
   'student(id, name), enroll(student_id, course_id)',
   'id | name', 1),
  (104, '窗口函数排名',
   '使用窗口函数计算每门课程内的成绩排名，结果列含 student_id, course, score, rank_no。',
   'HARD', 'WINDOW,RANK',
   'CREATE TABLE score(student_id INT, course VARCHAR(32), score INT); INSERT INTO score VALUES (1,''数据库'',92),(2,''数据库'',75),(3,''数据库'',88),(1,''操作系统'',85),(2,''操作系统'',70);',
   'SELECT student_id, course, score, RANK() OVER (PARTITION BY course ORDER BY score DESC) AS rank_no FROM score;',
   'score(student_id, course, score)',
   'student_id | course | score | rank_no', 1)
ON DUPLICATE KEY UPDATE title = VALUES(title);

-- 历史提交（供 dashboard 立刻有数据）
INSERT INTO submission(id, user_id, problem_id, sql_content, status, score, runtime_ms, message, submitted_at) VALUES
  (1001, 2, 101, 'SELECT s.name, c.course, c.score FROM student s JOIN score c ON s.id = c.student_id WHERE c.score >= 80', 'ACCEPTED', 100, 32, 'Result set matched.',         NOW() - INTERVAL 2 DAY),
  (1002, 2, 102, 'SELECT course, AVG(score) FROM score GROUP BY course',                                                  'WRONG_ANSWER', 40, 41, '列别名缺失 avg_score',           NOW() - INTERVAL 1 DAY),
  (1003, 3, 103, 'SELECT s.id, s.name FROM student s LEFT JOIN enroll e ON s.id = e.student_id WHERE e.student_id IS NULL', 'TIME_LIMIT', 0, 3000, '查询执行超过时间限制',            NOW() - INTERVAL 6 HOUR),
  (1004, 4, 104, 'SELECT student_id, course, score, ROW_NUMBER() OVER () AS rank_no FROM score',                            'RUNTIME_ERROR', 0, 59, '排序未指定 PARTITION BY',         NOW() - INTERVAL 3 HOUR)
ON DUPLICATE KEY UPDATE status = VALUES(status);

-- 活跃记录种子
INSERT INTO student_activity(user_id, activity_date, submission_count, accepted_count) VALUES
  (2, CURDATE() - INTERVAL 2 DAY, 1, 1),
  (2, CURDATE() - INTERVAL 1 DAY, 1, 0),
  (3, CURDATE(),                  1, 0),
  (4, CURDATE(),                  1, 0)
ON DUPLICATE KEY UPDATE submission_count = VALUES(submission_count);

-- =================== 沙箱库 ===================
-- 沙箱执行用账户：可读可写自己的库，不能访问主业务库。
-- 这里只是预留一个空库，sandbox-service 会按题目的 init_sql 临时建表执行。
USE student_oj_sandbox;

CREATE USER IF NOT EXISTS 'sandbox'@'%' IDENTIFIED BY 'sandbox123';
GRANT ALL PRIVILEGES ON student_oj_sandbox.* TO 'sandbox'@'%';
FLUSH PRIVILEGES;
