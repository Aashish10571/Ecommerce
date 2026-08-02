package com.ecommerce.backend.integration.mail.consumer;

import com.ecommerce.backend.integration.mail.dto.MailEventPayload;
import com.ecommerce.backend.integration.mail.rabbitmq.MailQueueConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailEventListener {

    private final JavaMailSender javaMailSender;

    @RabbitListener(queues = MailQueueConfig.MAIL_QUEUE)
    public void handleMailEvent(MailEventPayload eventPayload) {
        log.info("Processing mail event for {}", eventPayload.to());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(eventPayload.to());
        message.setSubject(eventPayload.subject());
        message.setText(eventPayload.body());

        javaMailSender.send(message);
    }
}
