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
    private static final String USER_TOKEN_KEY_PREFIX = "auth:user-token:";

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper = new ObjectMapper();
    private final long ttlSeconds;
    private final ConcurrentHashMap<String, Entry> memory = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, UserTokenEntry> userTokens = new ConcurrentHashMap<>();

    public TokenStore(@Autowired(required = false) StringRedisTemplate redis,
                      @Value("${studentoj.auth.token.ttl-seconds:86400}") long ttlSeconds) {
        this.redis = redis;
        this.ttlSeconds = ttlSeconds;
    }

    public String issue(LoginResponse user) {
        String token = UUID.randomUUID().toString().replace("-", "");
        LoginResponse userWithToken = withToken(user, token);
        String payload = serialize(userWithToken);
        if (redis != null) {
            String currentToken = redis.opsForValue().get(userTokenKey(user.userId()));
            if (currentToken != null && !currentToken.equals(token)) {
                redis.delete(KEY_PREFIX + currentToken);
            }
            redis.opsForValue().set(KEY_PREFIX + token, payload, Duration.ofSeconds(ttlSeconds));
            redis.opsForValue().set(userTokenKey(user.userId()), token, Duration.ofSeconds(ttlSeconds));
        } else {
            Instant expireAt = Instant.now().plusSeconds(ttlSeconds);
            UserTokenEntry current = currentUserToken(user.userId());
            if (current != null && !current.token().equals(token)) {
                memory.remove(current.token());
            }
            memory.put(token, new Entry(payload, expireAt));
            userTokens.put(user.userId(), new UserTokenEntry(token, expireAt));
        }
        return token;
    }

    public LoginResponse resolve(String token) {
        LoginResponse user = readToken(token);
        if (user == null) {
            return null;
        }
        String currentToken = currentToken(user.userId());
        if (!token.equals(currentToken)) {
            deleteToken(token);
            return null;
        }
        return user;
    }

    private LoginResponse readToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String raw;
        if (redis != null) {
            raw = redis.opsForValue().get(KEY_PREFIX + token);
        } else {
            Entry e = memory.get(token);
            if (e == null || e.expireAt().isBefore(Instant.now())) {
                memory.remove(token);
                return null;
            }
            raw = e.payload();
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

    public void update(String token, LoginResponse user) {
        if (token == null || token.isBlank()) {
            return;
        }
        LoginResponse userWithToken = withToken(user, token);
        String payload = serialize(userWithToken);
        if (redis != null) {
            Long ttl = redis.getExpire(KEY_PREFIX + token);
            long seconds = (ttl != null && ttl > 0) ? ttl : ttlSeconds;
            redis.opsForValue().set(KEY_PREFIX + token, payload, Duration.ofSeconds(seconds));
            redis.opsForValue().set(userTokenKey(user.userId()), token, Duration.ofSeconds(seconds));
        } else {
            Entry existing = memory.get(token);
            Instant expireAt = existing != null ? existing.expireAt() : Instant.now().plusSeconds(ttlSeconds);
            memory.put(token, new Entry(payload, expireAt));
            userTokens.put(user.userId(), new UserTokenEntry(token, expireAt));
        }
    }

    public void revoke(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        LoginResponse user = readToken(token);
        if (redis != null) {
            redis.delete(KEY_PREFIX + token);
            if (user != null && token.equals(redis.opsForValue().get(userTokenKey(user.userId())))) {
                redis.delete(userTokenKey(user.userId()));
            }
        } else {
            memory.remove(token);
            if (user != null) {
                userTokens.computeIfPresent(user.userId(),
                        (ignored, current) -> token.equals(current.token()) ? null : current);
            }
        }
    }

    public void revokeUser(Long userId) {
        if (userId == null || userId <= 0) {
            return;
        }
        if (redis != null) {
            String currentToken = redis.opsForValue().get(userTokenKey(userId));
            if (currentToken != null) {
                redis.delete(KEY_PREFIX + currentToken);
            }
            redis.delete(userTokenKey(userId));
        } else {
            UserTokenEntry current = userTokens.remove(userId);
            if (current != null) {
                memory.remove(current.token());
            }
        }
    }

    private void deleteToken(String token) {
        if (redis != null) {
            redis.delete(KEY_PREFIX + token);
        } else {
            memory.remove(token);
        }
    }

    private String currentToken(Long userId) {
        if (userId == null) {
            return null;
        }
        if (redis != null) {
            return redis.opsForValue().get(userTokenKey(userId));
        }
        UserTokenEntry current = currentUserToken(userId);
        return current == null ? null : current.token();
    }

    private UserTokenEntry currentUserToken(Long userId) {
        UserTokenEntry current = userTokens.get(userId);
        if (current == null) {
            return null;
        }
        if (current.expireAt().isBefore(Instant.now())) {
            userTokens.remove(userId);
            memory.remove(current.token());
            return null;
        }
        return current;
    }

    private String serialize(LoginResponse user) {
        try {
            return mapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize token payload", e);
        }
    }

    private LoginResponse withToken(LoginResponse user, String token) {
        return new LoginResponse(
                token,
                user.userId(),
                user.username(),
                user.role(),
                user.realName(),
                user.studentNo(),
                user.email(),
                user.groupName(),
                user.status()
        );
    }

    private String userTokenKey(Long userId) {
        return USER_TOKEN_KEY_PREFIX + userId;
    }

    private record Entry(String payload, Instant expireAt) {
    }

    private record UserTokenEntry(String token, Instant expireAt) {
    }
}
