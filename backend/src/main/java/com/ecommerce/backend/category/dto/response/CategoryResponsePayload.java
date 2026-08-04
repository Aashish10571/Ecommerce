package com.ecommerce.backend.category.dto.response;

import java.util.UUID;

public record CategoryResponsePayload(
        UUID id,
        String name,
        String slug
) { }
