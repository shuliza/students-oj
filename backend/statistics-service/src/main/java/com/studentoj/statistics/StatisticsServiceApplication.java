package com.studentoj.statistics;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.studentoj.statistics.mapper")
@SpringBootApplication
public class StatisticsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(StatisticsServiceApplication.class, args);
    }
}
