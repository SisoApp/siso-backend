package com.siso.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 설정 클래스
 *
 * - 채팅 메시지 큐
 * - AI 매칭 큐
 */
@Configuration
public class RabbitMQConfig {

    // ===========================
    // 채팅 메시지 큐 설정
    // ===========================
    public static final String CHAT_EXCHANGE = "chat.exchange";
    public static final String CHAT_QUEUE = "chat.queue";
    public static final String CHAT_ROUTING_KEY = "chat.message";

    @Bean
    public TopicExchange chatExchange() {
        return new TopicExchange(CHAT_EXCHANGE);
    }

    @Bean
    public Queue chatQueue() {
        return QueueBuilder.durable(CHAT_QUEUE)
                .withArgument("x-message-ttl", 86400000)  // 24시간 TTL
                .withArgument("x-max-length", 10000)       // 최대 10,000개 메시지
                .build();
    }

    @Bean
    public Binding chatBinding(Queue chatQueue, TopicExchange chatExchange) {
        return BindingBuilder.bind(chatQueue)
                .to(chatExchange)
                .with(CHAT_ROUTING_KEY);
    }

    // ===========================
    // AI 매칭 큐 설정
    // ===========================
    public static final String MATCHING_EXCHANGE = "matching.exchange";
    public static final String MATCHING_QUEUE = "matching.queue";
    public static final String MATCHING_ROUTING_KEY = "matching.request";

    @Bean
    public TopicExchange matchingExchange() {
        return new TopicExchange(MATCHING_EXCHANGE);
    }

    @Bean
    public Queue matchingQueue() {
        return QueueBuilder.durable(MATCHING_QUEUE)
                .withArgument("x-message-ttl", 300000)  // 5분 TTL
                .withArgument("x-max-length", 1000)      // 최대 1,000개 매칭 요청
                .build();
    }

    @Bean
    public Binding matchingBinding(Queue matchingQueue, TopicExchange matchingExchange) {
        return BindingBuilder.bind(matchingQueue)
                .to(matchingExchange)
                .with(MATCHING_ROUTING_KEY);
    }

    // ===========================
    // Jackson 메시지 컨버터 (JSON 직렬화)
    // ===========================
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
