package com.ecommerce.backend.product.service;

import com.ecommerce.backend.product.dto.request.ProductSizeRequestPayload;
import com.ecommerce.backend.product.dto.response.ProductSizeResponsePayload;

import java.util.List;
import java.util.UUID;

public interface ProductSizeService {
    List<ProductSizeResponsePayload> getAllSizes();

    ProductSizeResponsePayload getSizeById(UUID id);

    ProductSizeResponsePayload createSize(
            ProductSizeRequestPayload requestPayload
    );

    ProductSizeResponsePayload updateSize(
            UUID id,
            ProductSizeRequestPayload requestPayload
    );

    void deleteSize(UUID id);
}
