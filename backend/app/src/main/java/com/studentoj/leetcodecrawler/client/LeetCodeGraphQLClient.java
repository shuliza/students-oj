package com.studentoj.leetcodecrawler.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentoj.leetcodecrawler.config.LeetCodeCrawlerProperties;
import com.studentoj.leetcodecrawler.dto.LeetCodeProblemDetail;
import com.studentoj.leetcodecrawler.dto.LeetCodeProblemSummary;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeetCodeGraphQLClient implements LeetCodeClient {
    private static final String PROBLEM_LIST_QUERY = """
            query problemsetQuestionList($categorySlug: String, $limit: Int, $skip: Int, $filters: QuestionFilterInput) {
              problemsetQuestionListV2(categorySlug: $categorySlug, limit: $limit, skip: $skip, filters: $filters) {
                totalLength
                hasMore
                questions {
                  title
                  translatedTitle
                  titleSlug
                  difficulty
                  paidOnly
                  topicTags { name slug }
                }
              }
            }
            """;

    private static final String PROBLEM_DETAIL_QUERY = """
            query questionData($titleSlug: String!) {
              question(titleSlug: $titleSlug) {
                questionId
                title
                translatedTitle
                titleSlug
                content
                translatedContent
                difficulty
                hints
                mysqlSchemas
                topicTags { name slug }
                codeSnippets { lang langSlug code }
              }
            }
            """;

    private final RestClient leetCodeRestClient;
    private final ObjectMapper objectMapper;
    private final LeetCodeCrawlerProperties properties;

    @Override
    @Retryable(retryFor = RuntimeException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public List<LeetCodeProblemSummary> fetchDatabaseProblems() {
        List<LeetCodeProblemSummary> results = new ArrayList<>();
        int skip = 0;
        int total = Integer.MAX_VALUE;
        while (skip < total) {
            JsonNode root = execute(PROBLEM_LIST_QUERY, Map.of(
                    "categorySlug", properties.categorySlug(),
                    "limit", properties.pageSize(),
                    "skip", skip,
                    "filters", Map.of(
                            "filterCombineType", "ALL",
                            "topicFilter", Map.of(
                                    "topicSlugs", List.of("database"),
                                    "operator", "IS"
                            )
                    )
            ));
            JsonNode listNode = root.path("data").path("problemsetQuestionListV2");
            total = listNode.path("totalLength").asInt(0);
            JsonNode questions = listNode.path("questions");
            for (JsonNode question : questions) {
                List<String> tags = readTags(question.path("topicTags"));
                LeetCodeProblemSummary summary = new LeetCodeProblemSummary(
                        firstText(question.path("translatedTitle"), question.path("title")),
                        question.path("titleSlug").asText(),
                        question.path("difficulty").asText(),
                        tags,
                        question.path("paidOnly").asBoolean(false)
                );
                if (!summary.paidOnly() && summary.isSqlProblem()) {
                    results.add(summary);
                }
            }
            skip += properties.pageSize();
            randomDelay();
        }
        return results;
    }

    @Override
    @Retryable(retryFor = RuntimeException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public LeetCodeProblemDetail fetchProblemDetail(String titleSlug) {
        JsonNode question = execute(PROBLEM_DETAIL_QUERY, Map.of("titleSlug", titleSlug))
                .path("data")
                .path("question");
        if (question.isMissingNode() || question.isNull()) {
            throw new IllegalStateException("LeetCode question not found: " + titleSlug);
        }
        List<LeetCodeProblemDetail.CodeSnippet> snippets = new ArrayList<>();
        for (JsonNode snippet : question.path("codeSnippets")) {
            snippets.add(new LeetCodeProblemDetail.CodeSnippet(
                    snippet.path("lang").asText(),
                    snippet.path("langSlug").asText(),
                    snippet.path("code").asText()
            ));
        }
        List<String> hints = new ArrayList<>();
        for (JsonNode hint : question.path("hints")) {
            hints.add(hint.asText());
        }
        randomDelay();
        return new LeetCodeProblemDetail(
                firstText(question.path("translatedTitle"), question.path("title")),
                question.path("titleSlug").asText(),
                question.path("difficulty").asText(),
                firstText(question.path("translatedContent"), question.path("content")),
                readTags(question.path("topicTags")),
                hints,
                snippets,
                readMysqlSchemas(question.path("mysqlSchemas"))
        );
    }

    JsonNode execute(String query, Map<String, Object> variables) {
        Map<String, Object> body = new HashMap<>();
        body.put("query", query);
        body.put("variables", variables);
        byte[] payloadBytes = leetCodeRestClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .headers(headers -> {
                    if (StringUtils.hasText(properties.cookie())) {
                        headers.add("Cookie", properties.cookie());
                    }
                })
                .body(body)
                .exchange((request, response) -> {
                    if (response.getStatusCode().isError()) {
                        throw new IllegalStateException("LeetCode GraphQL HTTP error: " + response.getStatusCode());
                    }
                    return response.getBody().readAllBytes();
                });
        String payload = new String(payloadBytes == null ? new byte[0] : payloadBytes, StandardCharsets.UTF_8);
        try {
            JsonNode root = objectMapper.readTree(payload);
            JsonNode errors = root.path("errors");
            if (errors.isArray() && !errors.isEmpty()) {
                throw new IllegalStateException("LeetCode GraphQL errors: " + errors);
            }
            return root;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse LeetCode GraphQL response", ex);
        }
    }

    private List<String> readTags(JsonNode nodes) {
        List<String> tags = new ArrayList<>();
        for (JsonNode node : nodes) {
            String name = node.path("name").asText();
            if ("Database".equalsIgnoreCase(name)) {
                name = "数据库";
            }
            if (StringUtils.hasText(name)) {
                tags.add(name);
            }
        }
        return tags;
    }

    private String readMysqlSchemas(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return "";
        }
        if (node.isArray()) {
            List<String> statements = new ArrayList<>();
            for (JsonNode item : node) {
                String value = item.asText("");
                if (StringUtils.hasText(value)) {
                    statements.add(value.trim());
                }
            }
            return String.join(";\n", statements);
        }
        return node.asText("");
    }

    private String firstText(JsonNode preferred, JsonNode fallback) {
        String preferredText = preferred == null ? "" : preferred.asText("");
        if (StringUtils.hasText(preferredText)) {
            return preferredText;
        }
        return fallback == null ? "" : fallback.asText("");
    }

    private void randomDelay() {
        long min = Math.max(0, properties.requestMinDelayMs());
        long max = Math.max(min, properties.requestMaxDelayMs());
        long sleepMillis = ThreadLocalRandom.current().nextLong(min, max + 1);
        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Crawler request delay interrupted", ex);
        }
    }
}
