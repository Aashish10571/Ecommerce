package com.ecommerce.backend.cart.dto.response;

import com.ecommerce.backend.product.dto.response.ProductVariantResponsePayload;

import java.time.LocalDateTime;
import java.util.UUID;

public record CartItemsResponsePayload(
        UUID id,
        ProductVariantResponsePayload variant,
        Integer quantity,
        LocalDateTime createdAt
) { }
