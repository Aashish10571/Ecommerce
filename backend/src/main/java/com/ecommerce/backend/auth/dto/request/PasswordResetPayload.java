package com.ecommerce.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetPayload(
        @NotBlank String email,
        @NotBlank String code,
        @NotBlank String newPassword
) { }
