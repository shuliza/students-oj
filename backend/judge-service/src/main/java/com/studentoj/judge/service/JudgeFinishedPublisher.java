package com.studentoj.judge.service;

import com.studentoj.judge.dto.JudgeFinishedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JudgeFinishedPublisher {

    private static final Logger log = LoggerFactory.getLogger(JudgeFinishedPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${studentoj.mq.exchange}")
    private String exchange;

    @Value("${studentoj.mq.judge-finished-routing-key}")
    private String routingKey;

    public JudgeFinishedPublisher(@Autowired(required = false) RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishFinished(JudgeFinishedEvent event) {
        if (rabbitTemplate == null) {
            log.warn("RabbitTemplate not available, skip publishing judge.finished for submission {}", event.submissionId());
            return;
        }
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
