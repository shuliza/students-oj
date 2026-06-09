package com.studentoj.auth.dto;

public record LoginRequest(String username, String password, String role) {
}
