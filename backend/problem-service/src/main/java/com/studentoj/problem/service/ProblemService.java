package com.studentoj.problem.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.studentoj.problem.dto.ProblemResponse;
import com.studentoj.problem.dto.SubmissionCreatedEvent;
import com.studentoj.problem.dto.SubmissionRequest;
import com.studentoj.problem.dto.SubmissionResponse;
import com.studentoj.problem.entity.ProblemEntity;
import com.studentoj.problem.entity.SubmissionEntity;
import com.studentoj.problem.mapper.ProblemMapper;
import com.studentoj.problem.mapper.SubmissionMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProblemService {

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ProblemMapper problemMapper;
    private final SubmissionMapper submissionMapper;
    private final SubmissionPublisher publisher;

    public ProblemService(ProblemMapper problemMapper, SubmissionMapper submissionMapper, SubmissionPublisher publisher) {
        this.problemMapper = problemMapper;
        this.submissionMapper = submissionMapper;
        this.publisher = publisher;
    }

    public List<ProblemResponse> list(Long viewerUserId) {
        List<ProblemEntity> problems = problemMapper.selectList(new QueryWrapper<ProblemEntity>().eq("status", 1).orderByAsc("id"));
        Map<Long, Map<String, Object>> stats = indexStats();
        Map<Long, Integer> userState = userStateMap(viewerUserId);

        return problems.stream().map(p -> toResponse(p, stats.get(p.getId()), userState.get(p.getId()))).toList();
    }

    public ProblemResponse detail(Long id, Long viewerUserId) {
        ProblemEntity entity = problemMapper.selectById(id);
        if (entity == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "题目不存在");
        }
        Map<Long, Map<String, Object>> stats = indexStats();
        Map<Long, Integer> userState = userStateMap(viewerUserId);
        return toResponse(entity, stats.get(entity.getId()), userState.get(entity.getId()));
    }

    public SubmissionResponse submit(SubmissionRequest request) {
        if (request == null || request.problemId() == null || request.sqlContent() == null || request.sqlContent().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "提交内容不完整");
        }
        ProblemEntity problem = problemMapper.selectById(request.problemId());
        if (problem == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "题目不存在");
        }

        Long userId = request.userId() == null ? 0L : request.userId();
        SubmissionEntity entity = new SubmissionEntity();
        entity.setUserId(userId);
        entity.setProblemId(request.problemId());
        entity.setSqlContent(request.sqlContent());
        entity.setStatus("PENDING");
        entity.setScore(0);
        entity.setRuntimeMs(0);
        entity.setMessage("已入队等待判题");
        entity.setSubmittedAt(LocalDateTime.now());
        submissionMapper.insert(entity);

        publisher.publishCreated(new SubmissionCreatedEvent(entity.getId(), userId, request.problemId(), request.sqlContent()));

        return new SubmissionResponse(
                entity.getId(),
                entity.getProblemId(),
                problem.getTitle(),
                entity.getUserId(),
                null,
                entity.getStatus(),
                entity.getScore(),
                entity.getRuntimeMs(),
                entity.getSubmittedAt().format(TS_FMT),
                entity.getMessage()
        );
    }

    public List<SubmissionResponse> recent(String groupName, Long studentId) {
        String normalizedGroupName = groupName == null || groupName.isBlank() ? null : groupName.trim();
        Long normalizedStudentId = studentId == null || studentId <= 0 ? null : studentId;
        return submissionMapper.selectRecent(normalizedGroupName, normalizedStudentId).stream().map(this::rowToSubmission).toList();
    }

    public List<SubmissionResponse> mine(Long userId) {
        if (userId == null || userId <= 0) {
            return List.of();
        }
        return submissionMapper.selectByUser(userId).stream().map(this::rowToSubmission).toList();
    }

    public SubmissionResponse getSubmission(Long id) {
        SubmissionEntity entity = submissionMapper.selectById(id);
        if (entity == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "提交记录不存在");
        }
        ProblemEntity problem = problemMapper.selectById(entity.getProblemId());
        return new SubmissionResponse(
                entity.getId(),
                entity.getProblemId(),
                problem == null ? null : problem.getTitle(),
                entity.getUserId(),
                null,
                entity.getStatus(),
                entity.getScore(),
                entity.getRuntimeMs(),
                entity.getSubmittedAt() == null ? null : entity.getSubmittedAt().format(TS_FMT),
                entity.getMessage()
        );
    }

    private ProblemResponse toResponse(ProblemEntity entity, Map<String, Object> stats, Integer userState) {
        List<String> tags = entity.getTags() == null || entity.getTags().isBlank()
                ? List.of()
                : Arrays.stream(entity.getTags().split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        int submissions = stats == null ? 0 : ((Number) stats.getOrDefault("submissions", 0)).intValue();
        int passRate = stats == null ? 0 : ((Number) stats.getOrDefault("pass_rate", 0)).intValue();
        String status = "TODO";
        if (userState != null) {
            status = userState == 2 ? "ACCEPTED" : "FAILED";
        }
        return new ProblemResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getDifficulty(),
                tags,
                passRate,
                submissions,
                status,
                entity.getDescription(),
                entity.getSampleInput(),
                entity.getSampleOutput()
        );
    }

    private SubmissionResponse rowToSubmission(Map<String, Object> row) {
        Object submittedAt = row.get("submitted_at");
        String submittedStr = null;
        if (submittedAt instanceof LocalDateTime ldt) {
            submittedStr = ldt.format(TS_FMT);
        } else if (submittedAt instanceof java.sql.Timestamp ts) {
            submittedStr = ts.toLocalDateTime().format(TS_FMT);
        } else if (submittedAt != null) {
            submittedStr = submittedAt.toString();
        }
        return new SubmissionResponse(
                ((Number) row.get("id")).longValue(),
                row.get("problem_id") == null ? null : ((Number) row.get("problem_id")).longValue(),
                (String) row.get("problem_title"),
                row.get("user_id") == null ? null : ((Number) row.get("user_id")).longValue(),
                (String) row.get("user_name"),
                (String) row.get("status"),
                row.get("score") == null ? 0 : ((Number) row.get("score")).intValue(),
                row.get("runtime_ms") == null ? 0 : ((Number) row.get("runtime_ms")).intValue(),
                submittedStr,
                (String) row.get("message")
        );
    }

    private Map<Long, Map<String, Object>> indexStats() {
        return problemMapper.selectStats().stream().collect(Collectors.toMap(
                row -> ((Number) row.get("problem_id")).longValue(),
                row -> row
        ));
    }

    private Map<Long, Integer> userStateMap(Long userId) {
        if (userId == null || userId <= 0) {
            return new HashMap<>();
        }
        return problemMapper.selectUserState(userId).stream().collect(Collectors.toMap(
                row -> ((Number) row.get("problem_id")).longValue(),
                row -> ((Number) row.get("best_state")).intValue()
        ));
    }
}
