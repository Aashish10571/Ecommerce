package com.ecommerce.backend.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductRequestPayload(
        @NotBlank String name,
        @NotBlank String description,
        @NotNull @Positive BigDecimal basePrice,
        @NotNull UUID categoryId,
        @NotEmpty List<ProductVariantRequestPayload> variants
) { }
