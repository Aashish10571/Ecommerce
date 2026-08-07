package com.ecommerce.backend.product.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductFilterRequestPayload(
        String keyword,
        UUID categoryId,
        UUID sizeId,
        UUID colorId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean inStockOnly
) { }
