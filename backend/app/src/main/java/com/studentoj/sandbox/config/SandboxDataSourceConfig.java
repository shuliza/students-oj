package com.studentoj.sandbox.config;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 双数据源配置。
 *
 * <p>一旦显式声明任何 DataSource bean，Spring Boot 就不再自动配置主数据源，
 * 因此这里必须把业务主库也显式声明出来，并标记为 {@link Primary}：MyBatis-Plus 及所有
 * 业务 Mapper 默认注入主库。
 *
 * <p>沙箱数据源与主库物理隔离：沙箱执行学生 SQL 需要 CREATE DATABASE / DROP DATABASE 等高权限操作，
 * 使用独立账号与独立库（student_oj_sandbox），避免学生 SQL 触及业务数据。
 * {@code SandboxService} 通过 {@code @Qualifier("sandboxDataSource")} 显式注入此数据源。
 */
@Configuration
public class SandboxDataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource dataSource(DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @ConfigurationProperties("studentoj.sandbox.datasource")
    public DataSource sandboxDataSource() {
        return new HikariDataSource();
    }
}
