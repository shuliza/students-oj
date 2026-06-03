package com.studentoj.sandbox.dto;

import java.util.List;
import java.util.Map;

public record SandboxExecuteResponse(
        String status,
        Integer runtimeMs,
        String message,
        Boolean match,
        List<String> columns,
        List<Map<String, Object>> rows
) {
}
