package com.studentoj.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentoj.auth.dto.LoginResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 令牌存储。
 * 生产 / 容器：使用 Redis（带 TTL）。
 * 单元测试 / 没有 Redis：自动退化为进程内 ConcurrentHashMap。
 */
@Component
public class TokenStore {

    private static final String KEY_PREFIX = "auth:token:";

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper = new ObjectMapper();
    private final long ttlSeconds;
    private final ConcurrentHashMap<String, Entry> memory = new ConcurrentHashMap<>();

    public TokenStore(@Autowired(required = false) StringRedisTemplate redis,
                      @Value("${auth.token.ttl-seconds:86400}") long ttlSeconds) {
        this.redis = redis;
        this.ttlSeconds = ttlSeconds;
    }

    public String issue(LoginResponse user) {
        String token = UUID.randomUUID().toString().replace("-", "");
        String payload;
        try {
            payload = mapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize token payload", e);
        }
        if (redis != null) {
            redis.opsForValue().set(KEY_PREFIX + token, payload, Duration.ofSeconds(ttlSeconds));
        } else {
            memory.put(token, new Entry(payload, Instant.now().plusSeconds(ttlSeconds)));
        }
        return token;
    }

    public LoginResponse resolve(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String raw;
        if (redis != null) {
            raw = redis.opsForValue().get(KEY_PREFIX + token);
        } else {
            Entry e = memory.get(token);
            if (e == null || e.expireAt.isBefore(Instant.now())) {
                memory.remove(token);
                return null;
            }
            raw = e.payload;
        }
        if (raw == null) {
            return null;
        }
        try {
            return mapper.readValue(raw, LoginResponse.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public void revoke(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        if (redis != null) {
            redis.delete(KEY_PREFIX + token);
        } else {
            memory.remove(token);
        }
    }

    private record Entry(String payload, Instant expireAt) {
    }
}
