package com.studentoj.sandbox.service;

import com.studentoj.sandbox.dto.SandboxExecuteRequest;
import com.studentoj.sandbox.dto.SandboxExecuteResponse;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SandboxService {

    private static final Logger log = LoggerFactory.getLogger(SandboxService.class);

    private static final Pattern DANGEROUS = Pattern.compile(
            "\\b(drop|truncate|alter|grant|revoke|shutdown|create\\s+user|set\\s+password|load_file|outfile|sleep\\s*\\(|benchmark\\s*\\()\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern TEMPORARY_TABLE = Pattern.compile(
            "(?i)^create\\s+temporary\\s+table\\s+(`?[a-zA-Z0-9_]+`?(?:\\.`?[a-zA-Z0-9_]+`?)?)(?=\\s|\\()");

    private final DataSource dataSource;

    @Value("${studentoj.sandbox.query-timeout-seconds:5}")
    private int queryTimeoutSeconds;

    @Value("${studentoj.sandbox.max-rows:5000}")
    private int maxRows;

    public SandboxService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public SandboxExecuteResponse execute(SandboxExecuteRequest request) {
        if (request == null || request.studentSql() == null || request.studentSql().isBlank()) {
            return new SandboxExecuteResponse("RUNTIME_ERROR", 0, "学生 SQL 不能为空", false);
        }
        String studentSql = request.studentSql().trim();
        if (!startsWithSelect(studentSql)) {
            return new SandboxExecuteResponse("WRONG_ANSWER", 0, "仅允许 SELECT 查询语句", false);
        }
        if (DANGEROUS.matcher(studentSql).find()) {
            return new SandboxExecuteResponse("WRONG_ANSWER", 0, "检测到危险关键字，已拒绝执行", false);
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                runInitSql(conn, request.initSql());

                long t0 = System.currentTimeMillis();
                List<List<Object>> studentRows = runQuery(conn, studentSql);
                long runtime = System.currentTimeMillis() - t0;

                List<List<Object>> answerRows = runQuery(conn, request.answerSql());

                boolean match = compareRows(studentRows, answerRows);
                String status = match ? "ACCEPTED" : "WRONG_ANSWER";
                String message = match
                        ? "结果集与参考答案一致"
                        : String.format("结果不匹配：你的结果 %d 行，参考结果 %d 行", studentRows.size(), answerRows.size());
                return new SandboxExecuteResponse(status, (int) runtime, message, match);
            } finally {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
        } catch (SQLException e) {
            log.warn("Sandbox SQL error: {}", e.getMessage());
            String status = "0".equals(String.valueOf(e.getSQLState())) ? "TIME_LIMIT_EXCEEDED" : "RUNTIME_ERROR";
            return new SandboxExecuteResponse(status, 0, "SQL 执行失败: " + e.getMessage(), false);
        } catch (Exception e) {
            log.error("Sandbox unexpected error", e);
            return new SandboxExecuteResponse("RUNTIME_ERROR", 0, "沙箱执行异常: " + e.getMessage(), false);
        }
    }

    private boolean startsWithSelect(String sql) {
        String lower = sql.toLowerCase();
        return lower.startsWith("select") || lower.startsWith("with") || lower.startsWith("(select");
    }

    private void runInitSql(Connection conn, String initSql) throws SQLException {
        if (initSql == null || initSql.isBlank()) {
            return;
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.setQueryTimeout(queryTimeoutSeconds);
            for (String piece : initSql.split(";")) {
                String s = toTemporaryTableSql(piece.trim());
                if (s.isEmpty()) {
                    continue;
                }
                String temporaryTable = temporaryTableName(s);
                if (temporaryTable != null) {
                    stmt.execute("DROP TEMPORARY TABLE IF EXISTS " + temporaryTable);
                }
                stmt.execute(s);
            }
        }
    }

    private String toTemporaryTableSql(String sql) {
        return sql.replaceFirst("(?i)^create\\s+table\\s+(if\\s+not\\s+exists\\s+)?", "CREATE TEMPORARY TABLE ");
    }

    private String temporaryTableName(String sql) {
        Matcher matcher = TEMPORARY_TABLE.matcher(sql);
        return matcher.find() ? matcher.group(1) : null;
    }

    private List<List<Object>> runQuery(Connection conn, String sql) throws SQLException {
        if (sql == null || sql.isBlank()) {
            return List.of();
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.setQueryTimeout(queryTimeoutSeconds);
            stmt.setMaxRows(maxRows);
            try (ResultSet rs = stmt.executeQuery(sql.trim())) {
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                List<List<Object>> rows = new ArrayList<>();
                while (rs.next()) {
                    List<Object> row = new ArrayList<>(cols);
                    for (int i = 1; i <= cols; i++) {
                        row.add(rs.getObject(i));
                    }
                    rows.add(row);
                }
                return rows;
            }
        }
    }

    private boolean compareRows(List<List<Object>> a, List<List<Object>> b) {
        if (a.size() != b.size()) {
            return false;
        }
        List<String> sa = a.stream().map(this::normalizeRow).sorted().toList();
        List<String> sb = b.stream().map(this::normalizeRow).sorted().toList();
        return sa.equals(sb);
    }

    private String normalizeRow(List<Object> row) {
        String[] parts = new String[row.size()];
        for (int i = 0; i < row.size(); i++) {
            Object o = row.get(i);
            parts[i] = o == null ? "<null>" : Objects.toString(o).trim();
        }
        return String.join("", Arrays.asList(parts));
    }
}
