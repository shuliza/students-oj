package com.studentoj.teacher;

import com.studentoj.common.auth.AuthWebMvcConfigurer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@MapperScan("com.studentoj.teacher.mapper")
@Import(AuthWebMvcConfigurer.class)
@SpringBootApplication
public class TeacherServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TeacherServiceApplication.class, args);
    }
}
