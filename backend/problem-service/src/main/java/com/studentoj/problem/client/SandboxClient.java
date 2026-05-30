package com.studentoj.problem.client;

import com.studentoj.problem.dto.SandboxExecuteRequest;
import com.studentoj.problem.dto.SandboxExecuteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * 试运行用：直接同步调用 sandbox 执行学生 SQL（沙箱内仍走 SQL 白名单/危险拦截），
 * 不落库、不进 MQ。判题正式流程仍由 judge-service 负责。
 */
@Component
public class SandboxClient {

    private static final Logger log = LoggerFactory.getLogger(SandboxClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public SandboxClient(RestTemplate sandboxRestTemplate,
                         @Value("${studentoj.sandbox.base-url:http://localhost:8084}") String baseUrl) {
        this.restTemplate = sandboxRestTemplate;
        this.baseUrl = baseUrl;
    }

    public SandboxExecuteResponse execute(SandboxExecuteRequest request) {
        String url = baseUrl + "/api/sandbox/sql/execute";
        try {
            return restTemplate.postForObject(url, request, SandboxExecuteResponse.class);
        } catch (RestClientException e) {
            log.error("Sandbox trial-run call failed: {}", e.getMessage());
            return new SandboxExecuteResponse("RUNTIME_ERROR", 0, "沙箱调用失败: " + e.getMessage(), false);
        }
    }
}
