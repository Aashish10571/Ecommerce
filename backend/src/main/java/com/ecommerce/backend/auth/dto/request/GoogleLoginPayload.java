package com.ecommerce.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginPayload(
        @NotBlank String googleToken
) { }
