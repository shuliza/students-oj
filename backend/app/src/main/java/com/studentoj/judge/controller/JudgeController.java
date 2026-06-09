package com.studentoj.judge.controller;

import com.studentoj.common.auth.RequireRole;
import com.studentoj.common.auth.Role;
import com.studentoj.judge.dto.JudgeRequest;
import com.studentoj.judge.dto.JudgeResult;
import com.studentoj.judge.service.JudgeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/judge")
public class JudgeController {

    private final JudgeService judgeService;

    public JudgeController(JudgeService judgeService) {
        this.judgeService = judgeService;
    }

    @PostMapping("/run")
    @RequireRole(Role.TEACHER)
    public JudgeResult judge(@RequestBody JudgeRequest request) {
        return judgeService.judge(request);
    }

    @PostMapping("/rejudge/{submissionId}")
    @RequireRole(Role.TEACHER)
    public JudgeResult rejudge(@PathVariable("submissionId") Long submissionId) {
        return judgeService.rejudge(submissionId);
    }

    @GetMapping("/health")
    public String health() {
        return "judge-service ok";
    }
}
