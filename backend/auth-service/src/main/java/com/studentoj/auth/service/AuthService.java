package com.studentoj.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.studentoj.auth.dto.LoginRequest;
import com.studentoj.auth.dto.LoginResponse;
import com.studentoj.auth.entity.UserEntity;
import com.studentoj.auth.mapper.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenStore tokenStore;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, TokenStore tokenStore) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenStore = tokenStore;
    }

    public LoginResponse login(LoginRequest request) {
        if (request == null || request.username() == null || request.password() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户名和密码不能为空");
        }

        UserEntity user = userMapper.selectOne(new QueryWrapper<UserEntity>().eq("username", request.username().trim()));
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户不存在");
        }
        if ("DISABLED".equalsIgnoreCase(user.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "账号已被禁用");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "密码错误");
        }
        if (request.role() != null && !request.role().isBlank() && !user.getRole().equalsIgnoreCase(request.role())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "当前账号身份为 " + user.getRole() + "，无法以 " + request.role() + " 身份登录");
        }

        LoginResponse payload = toResponse(null, user);
        String token = tokenStore.issue(payload);
        return toResponse(token, user);
    }

    public LoginResponse me(String token) {
        LoginResponse cached = tokenStore.resolve(token);
        if (cached == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "会话已过期");
        }
        return cached;
    }

    public void logout(String token) {
        tokenStore.revoke(token);
    }

    private LoginResponse toResponse(String token, UserEntity user) {
        String groupName = user.getGroupId() == null ? null : userMapper.selectGroupName(user.getGroupId());
        return new LoginResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getRealName(),
                user.getStudentNo(),
                user.getEmail(),
                groupName,
                user.getStatus()
        );
    }
}
