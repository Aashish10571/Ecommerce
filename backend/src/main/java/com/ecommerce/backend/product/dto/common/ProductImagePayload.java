package com.ecommerce.backend.product.dto.common;

import jakarta.validation.constraints.NotBlank;

public record ProductImagePayload (
        @NotBlank String imageUri,
        @NotBlank String altText,
        @NotBlank Integer displayOrder,
        @NotBlank boolean thumbnail
) { }
