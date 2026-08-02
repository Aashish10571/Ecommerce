package com.ecommerce.backend.integration.mail.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailQueueConfig {

    public static final String MAIL_EXCHANGE = "mail.exchange";
    public static final String MAIL_QUEUE = "mail.queue";
    public static final String MAIL_ROUTING_KEY = "mail.send";

    public static final String MAIL_DLX = "mail.dlx";
    public static final String MAIL_DLQ = "mail.dlq";
    public static final String MAIL_DL_ROUTING_KEY = "mail.send.failed";

    @Bean
    public TopicExchange mailExchange() {
        return new TopicExchange(MAIL_EXCHANGE);
    }

    @Bean
    public Queue mailQueue() {
        return QueueBuilder.durable(MAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", MAIL_DLX)
                .withArgument("x-dead-letter-routing-key", MAIL_DL_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding mailBinding(Queue mailQueue, TopicExchange mailExchange) {
        return BindingBuilder.bind(mailQueue).to(mailExchange).with(MAIL_ROUTING_KEY);
    }

    @Bean
    public DirectExchange mailDeadLetterExchange() {
        return new DirectExchange(MAIL_DLX);
    }

    @Bean
    public Queue mailDeadLetterQueue() {
        return QueueBuilder.durable(MAIL_DLQ).build();
    }

    @Bean
    public Binding mailDeadLetterBinding(Queue mailDeadLetterQueue, DirectExchange mailDeadLetterExchange) {
        return BindingBuilder.bind(mailDeadLetterQueue).to(mailDeadLetterExchange).with(MAIL_DL_ROUTING_KEY);
    }
}
