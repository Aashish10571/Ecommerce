package com.ecommerce.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshPayload(
        @NotBlank String refreshToken
) { }
