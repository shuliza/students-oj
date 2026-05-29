package com.studentoj.judge;

import com.studentoj.common.auth.AuthWebMvcConfigurer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@MapperScan("com.studentoj.judge.mapper")
@Import(AuthWebMvcConfigurer.class)
@SpringBootApplication
public class JudgeServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(JudgeServiceApplication.class, args);
    }
}
