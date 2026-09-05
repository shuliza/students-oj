package com.studentoj;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 单体应用统一入口，包含 auth / problem / judge / sandbox / ai / statistics / teacher /
 * leetcodecrawler 等业务模块。模块间通信均为进程内直接调用。
 *
 * <p>judge 与 problem 模块各自保留了映射同一张表的 Mapper（如 ProblemMapper / SubmissionMapper），
 * 简单类名相同。通过 nameGenerator 让 Mapper bean 以全限定类名命名，避免 bean 名冲突；
 * 业务代码按接口类型注入，不受影响。
 */
@SpringBootApplication(scanBasePackages = "com.studentoj")
@MapperScan(basePackages = "com.studentoj.**.mapper",
        nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class)
@ConfigurationPropertiesScan
@EnableScheduling
@EnableRetry
public class StudentOjApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentOjApplication.class, args);
    }
}
