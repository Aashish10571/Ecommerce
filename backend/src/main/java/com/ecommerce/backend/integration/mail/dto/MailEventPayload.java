package com.ecommerce.backend.integration.mail.dto;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public record MailEventPayload(
        @NotBlank String to,
        @NotBlank String subject,
        @NotBlank String body
) implements Serializable { }
