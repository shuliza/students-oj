package com.studentoj.judge.dto;

public record SandboxExecuteResponse(
        String status,
        Integer runtimeMs,
        String message,
        Boolean match
) {
}
