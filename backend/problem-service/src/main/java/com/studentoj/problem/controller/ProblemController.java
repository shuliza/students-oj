package com.studentoj.problem.controller;

import com.studentoj.problem.dto.ProblemResponse;
import com.studentoj.problem.dto.SubmissionRequest;
import com.studentoj.problem.dto.SubmissionResponse;
import com.studentoj.problem.service.ProblemService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @GetMapping("/api/problem/list")
    public List<ProblemResponse> list(HttpServletRequest request) {
        return problemService.list(extractUserId(request));
    }

    @GetMapping("/api/problem/{id}")
    public ProblemResponse detail(@PathVariable("id") Long id, HttpServletRequest request) {
        return problemService.detail(id, extractUserId(request));
    }

    @PostMapping("/api/submission/judge")
    public SubmissionResponse submit(@RequestBody SubmissionRequest request, HttpServletRequest http) {
        Long userId = request.userId();
        if (userId == null || userId <= 0) {
            userId = extractUserId(http);
        }
        return problemService.submit(new SubmissionRequest(userId, request.problemId(), request.sqlContent()));
    }

    @GetMapping("/api/submission/list")
    public List<SubmissionResponse> list(
            @RequestParam(value = "groupName", required = false) String groupName,
            @RequestParam(value = "studentId", required = false) Long studentId) {
        return problemService.recent(groupName, studentId);
    }

    @GetMapping("/api/submission/mine")
    public List<SubmissionResponse> mine(HttpServletRequest request) {
        return problemService.mine(extractUserId(request));
    }

    @GetMapping("/api/submission/{id}")
    public SubmissionResponse one(@PathVariable("id") Long id) {
        return problemService.getSubmission(id);
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
