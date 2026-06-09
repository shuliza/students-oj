package com.studentoj.leetcodecrawler.parser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SchemaParser {
    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile("(?i)(?:Table:\\s*)[`']?([A-Za-z][A-Za-z0-9_]*)[`']?");
    private static final Pattern INPUT_TABLE_PATTERN = Pattern.compile("(?m)^([A-Za-z][A-Za-z0-9_]*)\\s*=\\s*$");

    public SchemaParseResult parse(String html, String mysqlSchemas, String example) {
        if (StringUtils.hasText(mysqlSchemas)) {
            return new SchemaParseResult(mysqlSchemas.trim(), extractInsertSql(mysqlSchemas), mysqlSchemas.trim());
        }
        Document document = Jsoup.parse(html == null ? "" : html);
        Map<String, List<ColumnDef>> schemas = readSchemaTables(document);
        String createSql = buildCreateSql(schemas);
        String insertSql = buildInsertSql(example);
        return new SchemaParseResult(createSql, insertSql, createSql);
    }

    private Map<String, List<ColumnDef>> readSchemaTables(Document document) {
        Map<String, List<ColumnDef>> result = new LinkedHashMap<>();
        for (Element table : document.select("table")) {
            String tableName = findTableName(table);
            if (!StringUtils.hasText(tableName)) {
                continue;
            }
            List<ColumnDef> columns = readColumns(table);
            if (!columns.isEmpty()) {
                result.put(tableName, columns);
            }
        }
        return result;
    }

    private String findTableName(Element table) {
        Element cursor = table;
        for (int i = 0; i < 4 && cursor != null; i++) {
            Element previous = cursor.previousElementSibling();
            if (previous != null) {
                Matcher matcher = TABLE_NAME_PATTERN.matcher(previous.text());
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
            cursor = previous;
        }
        Matcher parentMatcher = TABLE_NAME_PATTERN.matcher(table.parent() == null ? "" : table.parent().text());
        return parentMatcher.find() ? parentMatcher.group(1) : "";
    }

    private List<ColumnDef> readColumns(Element table) {
        List<ColumnDef> columns = new ArrayList<>();
        List<Element> rows = table.select("tr");
        for (int i = 1; i < rows.size(); i++) {
            List<Element> cells = rows.get(i).select("td,th");
            if (cells.size() < 2) {
                continue;
            }
            String column = cells.get(0).text().replace("`", "").trim();
            String type = cells.get(1).text().trim();
            if (StringUtils.hasText(column) && StringUtils.hasText(type)) {
                columns.add(new ColumnDef(column, toMysqlType(type)));
            }
        }
        return columns;
    }

    private String buildCreateSql(Map<String, List<ColumnDef>> schemas) {
        List<String> statements = new ArrayList<>();
        for (Map.Entry<String, List<ColumnDef>> entry : schemas.entrySet()) {
            List<String> lines = new ArrayList<>();
            lines.add("CREATE TABLE " + entry.getKey() + " (");
            List<ColumnDef> columns = entry.getValue();
            for (int i = 0; i < columns.size(); i++) {
                ColumnDef column = columns.get(i);
                String suffix = i == columns.size() - 1 ? "" : ",";
                lines.add("    " + column.name() + " " + column.type() + suffix);
            }
            lines.add(");");
            statements.add(String.join(System.lineSeparator(), lines));
        }
        return String.join(System.lineSeparator() + System.lineSeparator(), statements);
    }

    private String buildInsertSql(String example) {
        if (!StringUtils.hasText(example)) {
            return "";
        }
        List<String> statements = new ArrayList<>();
        String[] blocks = example.split("(?m)(?=^[A-Za-z][A-Za-z0-9_]*\\s*=\\s*$)");
        for (String block : blocks) {
            Matcher nameMatcher = INPUT_TABLE_PATTERN.matcher(block);
            if (!nameMatcher.find()) {
                continue;
            }
            String tableName = nameMatcher.group(1);
            List<List<String>> rows = readAsciiRows(block);
            if (rows.size() < 2) {
                continue;
            }
            List<String> columns = rows.get(0);
            for (int i = 1; i < rows.size(); i++) {
                List<String> values = rows.get(i);
                if (values.size() != columns.size()) {
                    continue;
                }
                statements.add("INSERT INTO " + tableName + " (" + String.join(", ", columns) + ") VALUES (" +
                        values.stream().map(this::sqlLiteral).reduce((a, b) -> a + ", " + b).orElse("") + ");");
            }
        }
        return String.join(System.lineSeparator(), statements);
    }

    private List<List<String>> readAsciiRows(String text) {
        List<List<String>> rows = new ArrayList<>();
        for (String line : text.split("\\R")) {
            String trimmed = line.trim();
            if (!trimmed.startsWith("|") || !trimmed.endsWith("|")) {
                continue;
            }
            String[] cells = trimmed.substring(1, trimmed.length() - 1).split("\\|");
            List<String> row = new ArrayList<>();
            for (String cell : cells) {
                row.add(cell.trim());
            }
            rows.add(row);
        }
        return rows;
    }

    private String extractInsertSql(String mysqlSchemas) {
        List<String> inserts = new ArrayList<>();
        for (String statement : mysqlSchemas.split(";")) {
            String trimmed = statement.trim();
            if (trimmed.toLowerCase(Locale.ROOT).startsWith("insert ")) {
                inserts.add(trimmed + ";");
            }
        }
        return String.join(System.lineSeparator(), inserts);
    }

    private String toMysqlType(String sourceType) {
        String type = sourceType.toLowerCase(Locale.ROOT);
        if (type.contains("int")) {
            return "INT";
        }
        if (type.contains("date")) {
            return "DATE";
        }
        if (type.contains("decimal") || type.contains("float") || type.contains("double")) {
            return "DECIMAL(10,2)";
        }
        if (type.contains("bool")) {
            return "BOOLEAN";
        }
        return "VARCHAR(255)";
    }

    private String sqlLiteral(String value) {
        if (!StringUtils.hasText(value) || "null".equalsIgnoreCase(value)) {
            return "NULL";
        }
        if (value.matches("-?\\d+(\\.\\d+)?")) {
            return value;
        }
        return "'" + value.replace("'", "''") + "'";
    }

    public record SchemaParseResult(String schemaInfo, String sampleData, String executableSql) {
    }

    private record ColumnDef(String name, String type) {
    }
}
