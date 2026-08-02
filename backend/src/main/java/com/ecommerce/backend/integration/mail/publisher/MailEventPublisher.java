package com.ecommerce.backend.integration.mail.publisher;

import com.ecommerce.backend.integration.mail.dto.MailEventPayload;
import com.ecommerce.backend.integration.mail.rabbitmq.MailQueueConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(String to, String subject, String body) {
        MailEventPayload eventPayload = new MailEventPayload(to, subject, body);

        rabbitTemplate.convertAndSend(
                MailQueueConfig.MAIL_EXCHANGE,
                MailQueueConfig.MAIL_ROUTING_KEY,
                eventPayload
        );
    }
}
