package com.studentoj.problem.dto;

public record SandboxExecuteResponse(String status, Integer runtimeMs, String message, Boolean match) {
}
