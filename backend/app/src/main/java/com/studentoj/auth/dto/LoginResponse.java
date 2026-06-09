package com.studentoj.auth.dto;

public record LoginResponse(
        String token,
        Long userId,
        String username,
        String role,
        String realName,
        String studentNo,
        String email,
        String groupName,
        String status
) {
}
