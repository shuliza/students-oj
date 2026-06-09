package com.studentoj.leetcodecrawler.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class TestCaseGenerator {
    private final ObjectMapper objectMapper;

    public String generate(String schemaInfo, String sampleData, String expectedOutput, String contentText, String tags) {
        List<Map<String, String>> cases = new ArrayList<>();
        String baseInput = joinSql(schemaInfo, sampleData);
        cases.add(Map.of("input", baseInput, "output", nullToEmpty(expectedOutput)));

        String typeHints = (contentText + " " + tags).toUpperCase(Locale.ROOT);
        if (typeHints.contains("JOIN")) {
            cases.add(Map.of("input", baseInput + System.lineSeparator() + "-- JOIN edge case: unmatched rows should be handled by the submitted SQL.", "output", ""));
        }
        if (typeHints.contains("GROUP") || typeHints.contains("HAVING")) {
            cases.add(Map.of("input", baseInput + System.lineSeparator() + "-- GROUP BY edge case: duplicate groups and empty groups should be considered.", "output", ""));
        }
        if (typeHints.contains("ORDER")) {
            cases.add(Map.of("input", baseInput + System.lineSeparator() + "-- ORDER BY edge case: tied values should follow the problem statement.", "output", ""));
        }
        if (typeHints.contains("UNION")) {
            cases.add(Map.of("input", baseInput + System.lineSeparator() + "-- UNION edge case: duplicate rows should be considered.", "output", ""));
        }
        if (typeHints.contains("WINDOW") || typeHints.contains("RANK") || typeHints.contains("OVER")) {
            cases.add(Map.of("input", baseInput + System.lineSeparator() + "-- WINDOW edge case: partition ties should be considered.", "output", ""));
        }
        if (typeHints.contains("WITH") || typeHints.contains("CTE")) {
            cases.add(Map.of("input", baseInput + System.lineSeparator() + "-- CTE edge case: recursive or multi-step derivation should be considered.", "output", ""));
        }
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(cases);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to generate test case JSON", ex);
        }
    }

    private String joinSql(String schemaInfo, String sampleData) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(schemaInfo)) {
            builder.append(schemaInfo.trim());
        }
        if (StringUtils.hasText(sampleData)) {
            if (!builder.isEmpty()) {
                builder.append(System.lineSeparator());
            }
            builder.append(sampleData.trim());
        }
        return builder.toString();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
