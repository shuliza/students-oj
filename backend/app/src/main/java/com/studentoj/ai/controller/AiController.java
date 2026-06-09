package com.studentoj.ai.controller;

import com.studentoj.common.auth.AuthContext;
import com.studentoj.common.auth.RequireRole;
import com.studentoj.common.auth.Role;
import com.studentoj.ai.dto.AiSuggestionRequest;
import com.studentoj.ai.dto.AiSuggestionResponse;
import com.studentoj.ai.service.AiService;
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
    @RequireRole(Role.STUDENT)
    public AiSuggestionResponse suggestion(@RequestBody AiSuggestionRequest request) {
        return aiService.generate(AuthContext.userId(), request);
    }

    @GetMapping("/suggestion/history")
    @RequireRole(Role.STUDENT)
    public List<AiSuggestionResponse> history(@RequestParam("problemId") Long problemId) {
        return aiService.history(AuthContext.userId(), problemId);
    }
}
