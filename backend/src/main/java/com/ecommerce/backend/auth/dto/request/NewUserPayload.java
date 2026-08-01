package com.ecommerce.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record NewUserPayload(
        @NotBlank String username,
        @NotBlank String email
) { }
