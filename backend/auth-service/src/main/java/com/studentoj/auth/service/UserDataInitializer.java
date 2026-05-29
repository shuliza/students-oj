package com.studentoj.auth.service;

import com.studentoj.auth.entity.UserEntity;
import com.studentoj.auth.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 启动时给 init.sql 里 password_hash 为空的用户回填 BCrypt(123456)。
 * 这样 SQL 种子文件不需要预先生成哈希值，重启幂等。
 */
@Component
@Profile("!test")
@Order(1)
public class UserDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(UserDataInitializer.class);
    private static final String DEFAULT_PASSWORD = "123456";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserDataInitializer(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        int updated = 0;
        for (UserEntity user : userMapper.selectList(null)) {
            if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
                user.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
                userMapper.updateById(user);
                updated++;
            }
        }
        if (updated > 0) {
            log.info("BCrypt-initialized password hash for {} seed user(s) using default password.", updated);
        }
    }
}
