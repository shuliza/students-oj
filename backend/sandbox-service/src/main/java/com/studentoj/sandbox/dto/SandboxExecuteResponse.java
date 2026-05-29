package com.studentoj.sandbox.dto;

public record SandboxExecuteResponse(
        String status,
        Integer runtimeMs,
        String message,
        Boolean match
) {
}
