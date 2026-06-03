SET NAMES utf8mb4;

USE student_oj;

CREATE TABLE IF NOT EXISTS processed_judge_event (
  submission_id BIGINT   NOT NULL,
  processed_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (submission_id)
);

DROP PROCEDURE IF EXISTS add_index_if_missing;
DELIMITER //
CREATE PROCEDURE add_index_if_missing(
  IN p_index_name VARCHAR(64),
  IN p_index_ddl TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'submission'
      AND index_name = p_index_name
  ) THEN
    SET @ddl = p_index_ddl;
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//
DELIMITER ;

CALL add_index_if_missing(
  'idx_submission_user_problem_status_time',
  'ALTER TABLE submission ADD INDEX idx_submission_user_problem_status_time (user_id, problem_id, status, submitted_at)'
);
CALL add_index_if_missing(
  'idx_submission_problem_status_user',
  'ALTER TABLE submission ADD INDEX idx_submission_problem_status_user (problem_id, status, user_id)'
);
CALL add_index_if_missing(
  'idx_submission_user_time',
  'ALTER TABLE submission ADD INDEX idx_submission_user_time (user_id, submitted_at)'
);

DROP PROCEDURE add_index_if_missing;

USE student_oj_sandbox;

CREATE USER IF NOT EXISTS 'sandbox'@'%' IDENTIFIED BY 'sandbox123';
GRANT ALL PRIVILEGES ON student_oj_sandbox.* TO 'sandbox'@'%';
GRANT ALL PRIVILEGES ON `oj\_run\_%`.* TO 'sandbox'@'%';
FLUSH PRIVILEGES;
