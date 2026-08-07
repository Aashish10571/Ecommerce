package com.ecommerce.backend.product.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductVariantResponsePayload(
        UUID id,
        String sku,
        BigDecimal priceOverride,
        Integer stockQuantity,
        boolean available,
        ProductSizeResponsePayload sizePayload,
        ProductColorResponsePayload colorPayload
) { }
