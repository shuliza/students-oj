package com.studentoj.problem.service;

import com.studentoj.problem.dto.SubmissionCreatedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SubmissionPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public SubmissionPublisher(@Autowired(required = false) RabbitTemplate rabbitTemplate,
                               @Value("${studentoj.mq.exchange:student-oj.exchange}") String exchange,
                               @Value("${studentoj.mq.submission-created-routing-key:submission.created}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publishCreated(SubmissionCreatedEvent event) {
        if (rabbitTemplate == null) {
            return;
        }
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
