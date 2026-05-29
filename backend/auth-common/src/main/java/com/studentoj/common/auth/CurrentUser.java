package com.studentoj.common.auth;

public record CurrentUser(Long userId, String username, String role) {
}
