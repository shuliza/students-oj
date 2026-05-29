package com.studentoj.ai.service;

import com.studentoj.ai.dto.AiSuggestionRequest;
import com.studentoj.ai.dto.AiSuggestionResponse;
import com.studentoj.ai.entity.AiSuggestionEntity;
import com.studentoj.ai.mapper.AiSuggestionMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AiSuggestionMapper mapper;
    private final RuleSuggestionGenerator generator;

    public AiService(AiSuggestionMapper mapper, RuleSuggestionGenerator generator) {
        this.mapper = mapper;
        this.generator = generator;
    }

    public AiSuggestionResponse generate(Long userId, AiSuggestionRequest request) {
        String suggestion = generator.generate(request);

        AiSuggestionEntity entity = new AiSuggestionEntity();
        entity.setUserId(userId == null ? 0L : userId);
        entity.setSubmissionId(request.submissionId());
        entity.setProblemId(request.problemId());
        entity.setSuggestion(suggestion);
        entity.setCreatedAt(LocalDateTime.now());
        mapper.insert(entity);

        return toResponse(entity);
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
