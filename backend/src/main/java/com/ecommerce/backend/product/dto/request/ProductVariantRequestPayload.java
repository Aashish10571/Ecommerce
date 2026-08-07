package com.ecommerce.backend.product.dto.request;

import com.ecommerce.backend.product.dto.common.ProductImagePayload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductVariantRequestPayload(
        @NotBlank String sku,
        @NotBlank @Positive BigDecimal priceOverride,
        @NotBlank @Positive Integer stockQuantity,
        UUID sizeId,
        UUID colorId,
        List<ProductImagePayload> images
) { }
