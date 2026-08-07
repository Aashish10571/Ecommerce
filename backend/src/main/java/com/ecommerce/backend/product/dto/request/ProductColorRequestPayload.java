package com.ecommerce.backend.product.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ProductColorRequestPayload(
        @NotBlank String name,
        @NotBlank String hexCode
) { }
