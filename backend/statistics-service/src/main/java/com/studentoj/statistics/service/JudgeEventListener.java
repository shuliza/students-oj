package com.studentoj.statistics.service;

import com.studentoj.statistics.dto.JudgeFinishedEvent;
import com.studentoj.statistics.mapper.StudentActivityMapper;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class JudgeEventListener {

    private static final Logger log = LoggerFactory.getLogger(JudgeEventListener.class);

    private final StudentActivityMapper activityMapper;

    public JudgeEventListener(StudentActivityMapper activityMapper) {
        this.activityMapper = activityMapper;
    }

    @RabbitListener(queues = "${studentoj.mq.statistics-queue}")
    public void onJudgeFinished(JudgeFinishedEvent event) {
        if (event == null || event.userId() == null || event.userId() <= 0) {
            return;
        }
        try {
            activityMapper.upsertIncrement(event.userId(), LocalDate.now());
        } catch (Exception e) {
            log.warn("Failed to upsert activity for user {}: {}", event.userId(), e.getMessage());
        }
    }
}
