package com.studentoj.problem.service;

import com.studentoj.problem.dto.JudgeFinishedEvent;
import com.studentoj.problem.entity.SubmissionEntity;
import com.studentoj.problem.mapper.SubmissionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class JudgeFinishedListener {

    private static final Logger log = LoggerFactory.getLogger(JudgeFinishedListener.class);

    private final SubmissionMapper submissionMapper;

    public JudgeFinishedListener(SubmissionMapper submissionMapper) {
        this.submissionMapper = submissionMapper;
    }

    @RabbitListener(queues = "${studentoj.mq.problem-update-queue}")
    public void onJudgeFinished(JudgeFinishedEvent event) {
        if (event == null || event.submissionId() == null) {
            return;
        }
        SubmissionEntity entity = submissionMapper.selectById(event.submissionId());
        if (entity == null) {
            log.warn("Received judge.finished for unknown submission {}", event.submissionId());
            return;
        }
        entity.setStatus(event.status());
        entity.setScore(event.score() == null ? 0 : event.score());
        entity.setRuntimeMs(event.runtimeMs() == null ? 0 : event.runtimeMs());
        entity.setMessage(event.message());
        submissionMapper.updateById(entity);
    }
}
