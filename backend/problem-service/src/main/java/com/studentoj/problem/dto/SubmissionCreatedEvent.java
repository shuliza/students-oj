package com.studentoj.problem.dto;

/**
 * 发到 RabbitMQ 的提交事件。judge-service 消费后真正执行判题。
 */
public record SubmissionCreatedEvent(Long submissionId, Long userId, Long problemId, String sqlContent) {
}
