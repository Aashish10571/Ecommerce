package com.ecommerce.backend.product.dto.response;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record ProductSizeResponsePayload(
        UUID id,
        String label,
        Integer sortOrder
) { }
