package com.studentoj.judge.service;

import com.studentoj.judge.client.SandboxClient;
import com.studentoj.judge.dto.JudgeFinishedEvent;
import com.studentoj.judge.dto.JudgeRequest;
import com.studentoj.judge.dto.JudgeResult;
import com.studentoj.judge.dto.SandboxExecuteRequest;
import com.studentoj.judge.dto.SandboxExecuteResponse;
import com.studentoj.judge.entity.ProblemEntity;
import com.studentoj.judge.entity.ProblemTestcaseEntity;
import com.studentoj.judge.entity.SubmissionEntity;
import com.studentoj.judge.mapper.ProblemMapper;
import com.studentoj.judge.mapper.ProblemTestcaseMapper;
import com.studentoj.judge.mapper.SubmissionMapper;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JudgeService {

    private static final Logger log = LoggerFactory.getLogger(JudgeService.class);

    private final ProblemMapper problemMapper;
    private final ProblemTestcaseMapper testcaseMapper;
    private final SandboxClient sandboxClient;
    private final JudgeFinishedPublisher publisher;
    private final SubmissionMapper submissionMapper;

    public JudgeService(ProblemMapper problemMapper,
                        ProblemTestcaseMapper testcaseMapper,
                        SandboxClient sandboxClient,
                        JudgeFinishedPublisher publisher,
                        SubmissionMapper submissionMapper) {
        this.problemMapper = problemMapper;
        this.testcaseMapper = testcaseMapper;
        this.sandboxClient = sandboxClient;
        this.publisher = publisher;
        this.submissionMapper = submissionMapper;
    }

    public JudgeResult rejudge(Long submissionId) {
        if (submissionId == null) {
            return new JudgeResult(null, "RUNTIME_ERROR", 0, 0, "提交ID不能为空");
        }
        SubmissionEntity submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            return new JudgeResult(submissionId, "RUNTIME_ERROR", 0, 0, "提交记录不存在");
        }
        return judge(new JudgeRequest(submissionId, submission.getUserId(),
                submission.getProblemId(), submission.getSqlContent()));
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

        // 收集数据集：优先用 problem_testcase 行；没有任何用例时回退到 problem.init_sql 单例。
        List<String> initSqls = new ArrayList<>();
        for (ProblemTestcaseEntity tc : testcaseMapper.selectByProblem(problem.getId())) {
            if (tc.getInitSql() != null && !tc.getInitSql().isBlank()) {
                initSqls.add(tc.getInitSql());
            }
        }
        if (initSqls.isEmpty()) {
            initSqls.add(problem.getInitSql());
        }

        JudgeResult result = judgeAllTestcases(request, problem, initSqls);
        publishIfNeeded(request, result);
        return result;
    }

    private JudgeResult judgeAllTestcases(JudgeRequest request, ProblemEntity problem, List<String> initSqls) {
        int totalRuntime = 0;
        int total = initSqls.size();
        for (int i = 0; i < total; i++) {
            SandboxExecuteResponse resp = sandboxClient.execute(new SandboxExecuteRequest(
                    initSqls.get(i),
                    problem.getAnswerSql(),
                    request.sqlContent()
            ));

            String status = resp == null || resp.status() == null ? "RUNTIME_ERROR" : resp.status();
            totalRuntime += resp == null || resp.runtimeMs() == null ? 0 : resp.runtimeMs();

            // 任意用例未通过即整体失败，沿用该用例的状态并标注序号。
            if (!"ACCEPTED".equals(status)) {
                String detail = resp == null ? "沙箱无响应" : resp.message();
                String message = total > 1
                        ? String.format("测试用例 %d/%d 未通过：%s", i + 1, total, detail)
                        : detail;
                return new JudgeResult(request.submissionId(), status, 0, totalRuntime, message);
            }
        }

        String message = total > 1 ? String.format("全部 %d 个测试用例通过", total) : "结果集与参考答案一致";
        return new JudgeResult(request.submissionId(), "ACCEPTED", 100, totalRuntime, message);
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
