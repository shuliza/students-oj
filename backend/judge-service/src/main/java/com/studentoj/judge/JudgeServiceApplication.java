package com.studentoj.judge;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.studentoj.judge.mapper")
@SpringBootApplication
public class JudgeServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(JudgeServiceApplication.class, args);
    }
}
