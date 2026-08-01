package com.ecommerce.backend.auth.dto.response;

public record TokenResponsePayload(
        String accessToken,
        String refreshToken
) { }
