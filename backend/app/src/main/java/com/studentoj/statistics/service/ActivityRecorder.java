package com.studentoj.statistics.service;

import com.studentoj.statistics.mapper.StudentActivityMapper;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 学生活跃度记录入口。原先由 statistics 的 RabbitMQ 监听器在收到 judge.finished 后调用，
 * 单体化后改为判题完成时由 JudgeService 进程内直接调用。仍保留按 submission_id 幂等去重。
 */
@Service
public class ActivityRecorder {

    private static final Logger log = LoggerFactory.getLogger(ActivityRecorder.class);

    private final StudentActivityMapper activityMapper;

    public ActivityRecorder(StudentActivityMapper activityMapper) {
        this.activityMapper = activityMapper;
    }

    @Transactional
    public void record(Long submissionId, Long userId, LocalDate date, boolean accepted) {
        if (submissionId == null || userId == null || userId <= 0) {
            return;
        }
        // markProcessed 利用 processed_judge_event 唯一键做幂等：重复 submissionId 返回 0，直接跳过。
        if (activityMapper.markProcessed(submissionId) == 0) {
            return;
        }
        activityMapper.upsertIncrement(userId, date, accepted ? 1 : 0);
    }
}
