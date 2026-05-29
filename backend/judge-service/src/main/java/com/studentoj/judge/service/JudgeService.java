package com.studentoj.judge.service;

import com.studentoj.judge.client.SandboxClient;
import com.studentoj.judge.dto.JudgeFinishedEvent;
import com.studentoj.judge.dto.JudgeRequest;
import com.studentoj.judge.dto.JudgeResult;
import com.studentoj.judge.dto.SandboxExecuteRequest;
import com.studentoj.judge.dto.SandboxExecuteResponse;
import com.studentoj.judge.entity.ProblemEntity;
import com.studentoj.judge.mapper.ProblemMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JudgeService {

    private static final Logger log = LoggerFactory.getLogger(JudgeService.class);

    private final ProblemMapper problemMapper;
    private final SandboxClient sandboxClient;
    private final JudgeFinishedPublisher publisher;

    public JudgeService(ProblemMapper problemMapper, SandboxClient sandboxClient, JudgeFinishedPublisher publisher) {
        this.problemMapper = problemMapper;
        this.sandboxClient = sandboxClient;
        this.publisher = publisher;
    }

    public JudgeResult judge(JudgeRequest request) {
        if (request == null || request.problemId() == null || request.sqlContent() == null) {
            return new JudgeResult(request == null ? null : request.submissionId(), "RUNTIME_ERROR", 0, 0, "判题请求不完整");
        }

        ProblemEntity problem = problemMapper.selectById(request.problemId());
        if (problem == null) {
            JudgeResult fail = new JudgeResult(request.submissionId(), "RUNTIME_ERROR", 0, 0, "题目不存在");
            publishIfNeeded(request, fail);
            return fail;
        }

        SandboxExecuteResponse sandboxResp = sandboxClient.execute(new SandboxExecuteRequest(
                problem.getInitSql(),
                problem.getAnswerSql(),
                request.sqlContent()
        ));

        String status = sandboxResp == null || sandboxResp.status() == null ? "RUNTIME_ERROR" : sandboxResp.status();
        int runtimeMs = sandboxResp == null || sandboxResp.runtimeMs() == null ? 0 : sandboxResp.runtimeMs();
        int score = "ACCEPTED".equals(status) ? 100 : 0;
        String message = sandboxResp == null ? "沙箱无响应" : sandboxResp.message();

        JudgeResult result = new JudgeResult(request.submissionId(), status, score, runtimeMs, message);
        publishIfNeeded(request, result);
        return result;
    }

    private void publishIfNeeded(JudgeRequest request, JudgeResult result) {
        if (request.submissionId() == null) {
            return;
        }
        try {
            publisher.publishFinished(new JudgeFinishedEvent(
                    result.submissionId(),
                    request.userId(),
                    request.problemId(),
                    result.status(),
                    result.score(),
                    result.runtimeMs(),
                    result.message()
            ));
        } catch (Exception e) {
            log.warn("Failed to publish judge.finished for submission {}: {}", request.submissionId(), e.getMessage());
        }
    }
}
