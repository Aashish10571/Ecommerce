package com.ecommerce.backend.product.dto.common;

import jakarta.validation.constraints.NotBlank;

public record ProductColorPayload (
        @NotBlank String name,
        @NotBlank String hexCode
) { }
