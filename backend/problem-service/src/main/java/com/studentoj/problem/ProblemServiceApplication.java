package com.studentoj.problem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.studentoj.problem.mapper")
@SpringBootApplication
public class ProblemServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProblemServiceApplication.class, args);
    }
}
