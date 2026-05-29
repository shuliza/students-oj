package com.studentoj.ai.controller;

import com.studentoj.ai.dto.AiSuggestionRequest;
import com.studentoj.ai.dto.AiSuggestionResponse;
import com.studentoj.ai.service.AiService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/suggestion")
    public AiSuggestionResponse suggestion(@RequestBody AiSuggestionRequest request, HttpServletRequest http) {
        return aiService.generate(extractUserId(http), request);
    }

    @GetMapping("/suggestion/history")
    public List<AiSuggestionResponse> history(@RequestParam("problemId") Long problemId, HttpServletRequest http) {
        return aiService.history(extractUserId(http), problemId);
    }

    private Long extractUserId(HttpServletRequest request) {
        String header = request.getHeader("X-User-Id");
        if (header == null || header.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(header.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
