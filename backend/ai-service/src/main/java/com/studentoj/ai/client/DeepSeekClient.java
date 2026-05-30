package com.studentoj.ai.client;

import com.studentoj.ai.dto.AiSuggestionRequest;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * DeepSeek 大模型客户端（OpenAI 兼容协议：POST {base-url}/chat/completions）。
 * 调用失败或未配置 api-key 时抛异常，由 AiService 回退到规则生成器。
 */
@Component
public class DeepSeekClient {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekClient.class);

    private static final String SYSTEM_PROMPT = """
            你是一名数据库课程的 SQL 助教，面向中国高校学生。学生在线评测平台上提交了一道 SQL 题目。
            请根据题目要求、学生 SQL 和判题结果，用简体中文给出一段有针对性的学习建议（150 字以内）。
            要求：指出可能的问题方向和改进思路，启发学生自己修正，不要直接给出完整的正确答案 SQL。""";

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String model;
    private final String apiKey;

    public DeepSeekClient(RestTemplate aiRestTemplate,
                          @Value("${studentoj.ai.deepseek.base-url:https://api.deepseek.com}") String baseUrl,
                          @Value("${studentoj.ai.deepseek.model:deepseek-chat}") String model,
                          @Value("${studentoj.ai.deepseek.api-key:}") String apiKey) {
        this.restTemplate = aiRestTemplate;
        this.baseUrl = baseUrl;
        this.model = model;
        this.apiKey = apiKey;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /** 调用 DeepSeek 生成建议；失败时抛 RuntimeException，由调用方回退规则。 */
    @SuppressWarnings("unchecked")
    public String generate(AiSuggestionRequest request) {
        if (!isConfigured()) {
            throw new IllegalStateException("DeepSeek api-key 未配置");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "model", model,
                "stream", false,
                "temperature", 0.3,
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", buildUserPrompt(request))
                )
        );

        Map<String, Object> response = restTemplate.postForObject(
                baseUrl + "/chat/completions", new HttpEntity<>(body, headers), Map.class);

        String content = extractContent(response);
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("DeepSeek 返回内容为空");
        }
        return content.trim();
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<String, Object> response) {
        if (response == null) {
            return null;
        }
        Object choices = response.get("choices");
        if (!(choices instanceof List<?> list) || list.isEmpty()) {
            return null;
        }
        Object first = list.get(0);
        if (!(first instanceof Map<?, ?> choice)) {
            return null;
        }
        Object message = choice.get("message");
        if (!(message instanceof Map<?, ?> msg)) {
            return null;
        }
        Object content = msg.get("content");
        return content == null ? null : content.toString();
    }

    private String buildUserPrompt(AiSuggestionRequest request) {
        String status = firstNonBlank(request.status(), request.judgeStatus());
        String sql = firstNonBlank(request.studentSql(), request.sqlContent());
        StringBuilder sb = new StringBuilder();
        if (request.title() != null && !request.title().isBlank()) {
            sb.append("题目：").append(request.title()).append('\n');
        }
        sb.append("判题结果：").append(status.isBlank() ? "未知" : status).append('\n');
        if (request.errorMessage() != null && !request.errorMessage().isBlank()) {
            sb.append("判题信息：").append(request.errorMessage()).append('\n');
        }
        sb.append("学生提交的 SQL：\n").append(sql.isBlank() ? "(空)" : sql);
        return sb.toString();
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first.trim();
        }
        return second == null ? "" : second.trim();
    }
}
