package com.ecommerce.backend.product.dto.response;

import java.util.UUID;

public record ProductColorResponsePayload(
        UUID id,
        String name,
        String hexCode
) { }
