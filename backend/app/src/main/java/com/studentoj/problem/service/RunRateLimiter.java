package com.studentoj.problem.service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RunRateLimiter {

    private final ConcurrentMap<Long, AtomicLong> lastRunAt = new ConcurrentHashMap<>();

    @Value("${studentoj.submission.run-min-interval-ms:2500}")
    private long minIntervalMs;

    public boolean tryAcquire(Long userId) {
        if (userId == null || userId <= 0 || minIntervalMs <= 0) {
            return true;
        }
        long now = Instant.now().toEpochMilli();
        AtomicLong slot = lastRunAt.computeIfAbsent(userId, ignored -> new AtomicLong(0));
        while (true) {
            long previous = slot.get();
            if (now - previous < minIntervalMs) {
                return false;
            }
            if (slot.compareAndSet(previous, now)) {
                return true;
            }
        }
    }

    public Duration retryAfter(Long userId) {
        AtomicLong slot = userId == null ? null : lastRunAt.get(userId);
        if (slot == null) {
            return Duration.ZERO;
        }
        long elapsed = Instant.now().toEpochMilli() - slot.get();
        long remaining = Math.max(0, minIntervalMs - elapsed);
        return Duration.ofMillis(remaining);
    }
}
