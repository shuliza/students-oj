package com.studentoj.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.studentoj.ai.mapper")
@SpringBootApplication
public class AiServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiServiceApplication.class, args);
    }
}
