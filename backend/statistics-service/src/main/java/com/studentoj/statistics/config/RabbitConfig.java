package com.studentoj.statistics.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
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

    @Value("${studentoj.mq.statistics-queue}")
    private String statisticsQueue;

    @Value("${studentoj.mq.judge-finished-routing-key}")
    private String judgeFinishedRoutingKey;

    @Bean
    public TopicExchange studentOjExchange() {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    public Queue statisticsQueue() {
        return QueueBuilder.durable(statisticsQueue).build();
    }

    @Bean
    public Binding statisticsBinding() {
        return BindingBuilder.bind(statisticsQueue()).to(studentOjExchange()).with(judgeFinishedRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
