package com.studentoj.judge.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class RabbitConfig {

    @Value("${studentoj.mq.exchange}")
    private String exchange;

    @Value("${studentoj.mq.submission-created-queue}")
    private String submissionCreatedQueue;

    @Value("${studentoj.mq.submission-created-routing-key}")
    private String submissionCreatedRoutingKey;

    @Bean
    public TopicExchange studentOjExchange() {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    public Queue submissionCreatedQueue() {
        return QueueBuilder.durable(submissionCreatedQueue).build();
    }

    @Bean
    public Binding submissionCreatedBinding() {
        return BindingBuilder.bind(submissionCreatedQueue()).to(studentOjExchange()).with(submissionCreatedRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
