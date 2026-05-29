package com.studentoj.common.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AuthWebMvcConfigurer implements WebMvcConfigurer {
    private final String internalSecret;

    public AuthWebMvcConfigurer(@Value("${studentoj.auth.internal-secret:student-oj-internal-v1}") String internalSecret) {
        this.internalSecret = internalSecret;
    }

    @Bean
    public AuthInterceptor authInterceptor() {
        return new AuthInterceptor(internalSecret);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor()).addPathPatterns("/api/**");
    }
}
