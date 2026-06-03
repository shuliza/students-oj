package com.studentoj.sandbox.service;

import com.studentoj.sandbox.dto.SandboxExecuteRequest;
import com.studentoj.sandbox.dto.SandboxExecuteResponse;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import jakarta.annotation.PostConstruct;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.select.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SandboxService {

    private static final Logger log = LoggerFactory.getLogger(SandboxService.class);

    private static final Pattern DANGEROUS = Pattern.compile(
            "\\b(drop|delete|truncate|alter|grant|revoke|shutdown|create\\s+database|create\\s+user|set\\s+password|load\\s+data|outfile|load_file|sleep\\s*\\(|benchmark\\s*\\()\\b",
            Pattern.CASE_INSENSITIVE);

    private final DataSource dataSource;

    @Value("${studentoj.sandbox.query-timeout-seconds:3}")
    private int queryTimeoutSeconds;

    @Value("${studentoj.sandbox.max-rows:5000}")
    private int maxRows;

    @Value("${studentoj.sandbox.max-concurrent-executions:16}")
    private int maxConcurrentExecutions;

    @Value("${studentoj.sandbox.acquire-timeout-ms:1500}")
    private long acquireTimeoutMs;

    public SandboxService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Semaphore executionSlots;

    @PostConstruct
    void initConcurrencyLimiter() {
        executionSlots = new Semaphore(Math.max(1, maxConcurrentExecutions));
    }

    public SandboxExecuteResponse execute(SandboxExecuteRequest request) {
        boolean acquired = false;
        try {
            acquired = executionSlots.tryAcquire(Math.max(0, acquireTimeoutMs), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return response("RUNTIME_ERROR", 0, "Sandbox execution interrupted.", false, null);
        }
        if (!acquired) {
            return response("SYSTEM_BUSY", 0, "Sandbox is busy. Please retry later.", false, null);
        }
        try {
            return executeInternal(request);
        } finally {
            executionSlots.release();
        }
    }

    private SandboxExecuteResponse executeInternal(SandboxExecuteRequest request) {
        if (request == null || isBlank(request.studentSql())) {
            return response("RUNTIME_ERROR", 0, "Student SQL must not be empty.", false, null);
        }
        if (isBlank(request.answerSql())) {
            return response("RUNTIME_ERROR", 0, "Reference answer SQL is missing.", false, null);
        }

        String studentSql = trimTrailingSemicolon(request.studentSql());
        String validationError = validateStudentSql(studentSql);
        if (validationError != null) {
            return response("RUNTIME_ERROR", 0, validationError, false, null);
        }

        String databaseName = "oj_run_" + UUID.randomUUID().toString().replace("-", "");
        long started = System.currentTimeMillis();
        try (Connection admin = dataSource.getConnection()) {
            createDatabase(admin, databaseName);
            try (Connection runConnection = dataSource.getConnection()) {
                runConnection.setCatalog(databaseName);
                runConnection.setAutoCommit(false);
                executeSqlScript(runConnection, request.initSql());

                QueryResult studentResult = runQuery(runConnection, studentSql);
                QueryResult expectedResult = runQuery(runConnection, request.answerSql());
                int runtime = Math.toIntExact(Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - started));

                boolean match = compare(studentResult, expectedResult);
                if (match) {
                    return response("ACCEPTED", runtime, "Accepted.", true, studentResult);
                }
                String message = "Wrong answer. Your result has " + studentResult.rows().size()
                        + " row(s), expected " + expectedResult.rows().size() + " row(s).";
                return response("WRONG_ANSWER", runtime, message, false, studentResult);
            } finally {
                dropDatabaseQuietly(admin, databaseName);
            }
        } catch (SQLTimeoutException e) {
            log.warn("Sandbox query timeout: {}", e.getMessage());
            return response("TIME_LIMIT_EXCEEDED", queryTimeoutSeconds * 1000, "Query timed out.", false, null);
        } catch (SQLException e) {
            log.warn("Sandbox SQL error: {}", e.getMessage());
            return response("RUNTIME_ERROR", 0, "SQL execution failed: " + e.getMessage(), false, null);
        } catch (Exception e) {
            log.error("Sandbox unexpected error", e);
            return response("RUNTIME_ERROR", 0, "Sandbox execution failed: " + e.getMessage(), false, null);
        }
    }

    private String validateStudentSql(String sql) {
        if (DANGEROUS.matcher(sql).find()) {
            return "Dangerous SQL keyword detected.";
        }
        try {
            var statements = CCJSqlParserUtil.parseStatements(sql).getStatements();
            if (statements.size() != 1) {
                return "Only one SQL statement is allowed.";
            }
            final boolean[] select = {false};
            statements.get(0).accept(new StatementVisitorAdapter() {
                @Override
                public void visit(Select statement) {
                    select[0] = true;
                }
            });
            return select[0] ? null : "Only SELECT/WITH queries are allowed.";
        } catch (Exception e) {
            return "SQL parse failed: " + e.getMessage();
        }
    }

    private void createDatabase(Connection conn, String databaseName) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            statement.execute("CREATE DATABASE `" + databaseName + "` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci");
        }
    }

    private void dropDatabaseQuietly(Connection conn, String databaseName) {
        try (Statement statement = conn.createStatement()) {
            statement.execute("DROP DATABASE IF EXISTS `" + databaseName + "`");
        } catch (Exception e) {
            log.warn("Failed to drop sandbox database {}: {}", databaseName, e.getMessage());
        }
    }

    private void executeSqlScript(Connection conn, String sqlScript) throws SQLException {
        if (isBlank(sqlScript)) {
            return;
        }
        try (Statement statement = conn.createStatement()) {
            statement.setQueryTimeout(queryTimeoutSeconds);
            for (String piece : splitStatements(sqlScript)) {
                String sql = piece.trim();
                if (!sql.isEmpty()) {
                    statement.execute(sql);
                }
            }
        }
    }

    private QueryResult runQuery(Connection conn, String sql) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            statement.setQueryTimeout(queryTimeoutSeconds);
            statement.setMaxRows(maxRows);
            try (ResultSet rs = statement.executeQuery(trimTrailingSemicolon(sql))) {
                ResultSetMetaData meta = rs.getMetaData();
                int count = meta.getColumnCount();
                List<String> columns = new ArrayList<>(count);
                for (int i = 1; i <= count; i++) {
                    String label = meta.getColumnLabel(i);
                    columns.add(label == null || label.isBlank() ? meta.getColumnName(i) : label);
                }

                List<Map<String, Object>> rows = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= count; i++) {
                        row.put(columns.get(i - 1), normalizeValue(rs.getObject(i)));
                    }
                    rows.add(row);
                }
                return new QueryResult(columns, rows);
            }
        }
    }

    private boolean compare(QueryResult actual, QueryResult expected) {
        if (!actual.columns().equals(expected.columns())) {
            return false;
        }
        if (actual.rows().size() != expected.rows().size()) {
            return false;
        }
        for (int i = 0; i < actual.rows().size(); i++) {
            if (!actual.rows().get(i).equals(expected.rows().get(i))) {
                return false;
            }
        }
        return true;
    }

    private Object normalizeValue(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal.stripTrailingZeros().toPlainString();
        }
        if (value instanceof Number number) {
            return new BigDecimal(number.toString()).stripTrailingZeros().toPlainString();
        }
        return value;
    }

    private List<String> splitStatements(String sqlScript) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean singleQuoted = false;
        boolean doubleQuoted = false;
        for (int i = 0; i < sqlScript.length(); i++) {
            char ch = sqlScript.charAt(i);
            if (ch == '\'' && !doubleQuoted) {
                singleQuoted = !singleQuoted;
            } else if (ch == '"' && !singleQuoted) {
                doubleQuoted = !doubleQuoted;
            }
            if (ch == ';' && !singleQuoted && !doubleQuoted) {
                statements.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        if (!current.isEmpty()) {
            statements.add(current.toString());
        }
        return statements;
    }

    private SandboxExecuteResponse response(String status, int runtimeMs, String message, boolean match, QueryResult result) {
        List<String> columns = result == null ? List.of() : result.columns();
        List<Map<String, Object>> rows = result == null ? List.of() : result.rows();
        return new SandboxExecuteResponse(status, runtimeMs, message, match, columns, rows);
    }

    private String trimTrailingSemicolon(String sql) {
        String trimmed = Objects.toString(sql, "").trim();
        while (trimmed.endsWith(";")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
        }
        return trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private record QueryResult(List<String> columns, List<Map<String, Object>> rows) {
    }
}
