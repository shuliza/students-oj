package com.studentoj.judge.dto;

public record JudgeResult(Long submissionId, String status, Integer score, Integer runtimeMs, String message) {
}
