package com.studentoj.sandbox.controller;

import com.studentoj.sandbox.dto.SandboxExecuteRequest;
import com.studentoj.sandbox.dto.SandboxExecuteResponse;
import com.studentoj.sandbox.service.SandboxService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sandbox")
public class SandboxController {
    private final SandboxService sandboxService;

    public SandboxController(SandboxService sandboxService) {
        this.sandboxService = sandboxService;
    }

    @PostMapping("/sql/execute")
    public SandboxExecuteResponse execute(@RequestBody SandboxExecuteRequest request) {
        return sandboxService.execute(request);
    }

    @GetMapping("/health")
    public String health() {
        return "sandbox ok";
    }
}
