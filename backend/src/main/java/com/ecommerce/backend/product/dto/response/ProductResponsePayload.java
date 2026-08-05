package com.ecommerce.backend.product.dto.response;

import com.ecommerce.backend.category.dto.response.CategoryResponsePayload;
import com.ecommerce.backend.product.dto.common.ProductColorPayload;
import com.ecommerce.backend.product.dto.common.ProductImagePayload;
import com.ecommerce.backend.product.dto.common.ProductSizePayload;
import com.ecommerce.backend.product.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProductResponsePayload (
        UUID id,
        String name,
        String slug,
        String description,
        BigDecimal basePrice,
        ProductStatus status,
        CategoryResponsePayload category,
        List<ProductImagePayload> images,
        List<ProductSizePayload> sizes,
        List<ProductColorPayload> colors,
        List<ProductVariantResponsePayload> variants,
        LocalDateTime createdAt
) { }
