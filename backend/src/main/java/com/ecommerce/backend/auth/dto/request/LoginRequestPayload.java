package com.ecommerce.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestPayload(
        @NotBlank String email,
        @NotBlank String password
) { }
