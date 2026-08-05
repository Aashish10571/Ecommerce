package com.ecommerce.backend.product.dto.common;

import jakarta.validation.constraints.NotBlank;

public record ProductSizePayload (
        @NotBlank String label,
        @NotBlank Integer sortOrder
) { }
