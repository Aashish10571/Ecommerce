package com.ecommerce.backend.integration.mail;

import org.springframework.scheduling.annotation.Async;

public interface MailService {
    @Async
    void sendMail(String toEmail, String subject, String message);
}
