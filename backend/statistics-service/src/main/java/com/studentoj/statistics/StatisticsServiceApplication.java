package com.studentoj.statistics;

import com.studentoj.common.auth.AuthWebMvcConfigurer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@MapperScan("com.studentoj.statistics.mapper")
@Import(AuthWebMvcConfigurer.class)
@SpringBootApplication
public class StatisticsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(StatisticsServiceApplication.class, args);
    }
}
