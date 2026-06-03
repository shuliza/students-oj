package com.studentoj.auth.dto;

public record ChangePasswordRequest(String oldPassword, String newPassword) {
}
