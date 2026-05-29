package com.studentoj.judge.service;

import com.studentoj.judge.dto.JudgeRequest;
import com.studentoj.judge.dto.SubmissionCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class JudgeListener {

    private static final Logger log = LoggerFactory.getLogger(JudgeListener.class);

    private final JudgeService judgeService;

    public JudgeListener(JudgeService judgeService) {
        this.judgeService = judgeService;
    }

    @RabbitListener(queues = "${studentoj.mq.submission-created-queue}")
    public void onSubmissionCreated(SubmissionCreatedEvent event) {
        if (event == null || event.submissionId() == null) {
            return;
        }
        log.info("Judging submission {} (problem {})", event.submissionId(), event.problemId());
        judgeService.judge(new JudgeRequest(event.submissionId(), event.userId(), event.problemId(), event.sqlContent()));
    }
}
