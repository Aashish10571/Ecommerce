package com.ecommerce.backend.profile.dto.response;

import java.time.Instant;

public record ProfileResponsePayload(
        String username,
        String email,
        Instant createdAt
) { }
