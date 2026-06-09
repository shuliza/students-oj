package com.studentoj.ai.service;

import com.studentoj.ai.client.DeepSeekClient;
import com.studentoj.ai.dto.AiSuggestionRequest;
import com.studentoj.ai.dto.AiSuggestionResponse;
import com.studentoj.ai.entity.AiSuggestionEntity;
import com.studentoj.ai.mapper.AiSuggestionMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AiSuggestionMapper mapper;
    private final RuleSuggestionGenerator generator;
    private final DeepSeekClient deepSeekClient;
    private final String mode;

    public AiService(AiSuggestionMapper mapper,
                     RuleSuggestionGenerator generator,
                     DeepSeekClient deepSeekClient,
                     @Value("${studentoj.ai.mode:auto}") String mode) {
        this.mapper = mapper;
        this.generator = generator;
        this.deepSeekClient = deepSeekClient;
        this.mode = mode == null ? "auto" : mode.trim().toLowerCase();
    }

    public AiSuggestionResponse generate(Long userId, AiSuggestionRequest request) {
        String suggestion = resolveSuggestion(request);

        AiSuggestionEntity entity = new AiSuggestionEntity();
        entity.setUserId(userId == null ? 0L : userId);
        entity.setSubmissionId(request.submissionId());
        entity.setProblemId(request.problemId());
        entity.setSuggestion(suggestion);
        entity.setCreatedAt(LocalDateTime.now());
        mapper.insert(entity);

        return toResponse(entity);
    }

    /**
     * mode=deepseek 仅用大模型；mode=rule 仅用规则；mode=auto（默认）DeepSeek 优先、失败回退规则。
     */
    private String resolveSuggestion(AiSuggestionRequest request) {
        boolean useDeepSeek = ("deepseek".equals(mode) || "auto".equals(mode)) && deepSeekClient.isConfigured();
        if (useDeepSeek) {
            try {
                return deepSeekClient.generate(request);
            } catch (Exception e) {
                log.warn("DeepSeek 调用失败，回退规则生成器: {}", e.getMessage());
                if ("deepseek".equals(mode)) {
                    return "AI 服务暂时不可用，请稍后重试。系统提示：" + generator.generate(request);
                }
            }
        }
        return generator.generate(request);
    }

    public List<AiSuggestionResponse> history(Long userId, Long problemId) {
        if (userId == null || userId <= 0 || problemId == null || problemId <= 0) {
            return List.of();
        }
        return mapper.selectLatestByUserAndProblem(userId, problemId).stream()
                .map(this::toResponse)
                .toList();
    }

    private AiSuggestionResponse toResponse(AiSuggestionEntity entity) {
        return new AiSuggestionResponse(
                entity.getId(),
                entity.getUserId(),
                entity.getSubmissionId(),
                entity.getProblemId(),
                entity.getSuggestion(),
                entity.getCreatedAt() == null ? null : entity.getCreatedAt().format(TS_FMT)
        );
    }
}
