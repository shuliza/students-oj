package com.studentoj.judge.client;

import com.studentoj.judge.dto.SandboxExecuteRequest;
import com.studentoj.judge.dto.SandboxExecuteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class SandboxClient {

    private static final Logger log = LoggerFactory.getLogger(SandboxClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public SandboxClient(RestTemplate restTemplate,
                         @Value("${studentoj.sandbox.base-url:http://localhost:8084}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public SandboxExecuteResponse execute(SandboxExecuteRequest request) {
        String url = baseUrl + "/api/sandbox/sql/execute";
        try {
            return restTemplate.postForObject(url, request, SandboxExecuteResponse.class);
        } catch (RestClientException e) {
            log.error("Sandbox call failed: {}", e.getMessage());
            return new SandboxExecuteResponse("RUNTIME_ERROR", 0, "沙箱调用失败: " + e.getMessage(), false);
        }
    }
}
