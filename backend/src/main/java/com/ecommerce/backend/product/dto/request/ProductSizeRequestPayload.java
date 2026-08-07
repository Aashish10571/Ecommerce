package com.ecommerce.backend.product.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ProductSizeRequestPayload(
        @NotBlank String label,
        @NotBlank Integer sortOrder
) { }
