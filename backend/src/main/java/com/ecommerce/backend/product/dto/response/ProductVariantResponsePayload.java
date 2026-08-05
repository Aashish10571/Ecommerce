package com.ecommerce.backend.product.dto.response;

import com.ecommerce.backend.product.dto.common.ProductColorPayload;
import com.ecommerce.backend.product.dto.common.ProductSizePayload;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductVariantResponsePayload(
        UUID id,
        String sku,
        BigDecimal price,
        Integer stock,
        boolean available,
        ProductSizePayload sizePayload,
        ProductColorPayload colorPayload
) { }
