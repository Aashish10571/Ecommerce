package com.ecommerce.backend.product.service;

import com.ecommerce.backend.product.dto.request.ProductColorRequestPayload;
import com.ecommerce.backend.product.dto.response.ProductColorResponsePayload;

import java.util.List;
import java.util.UUID;

public interface ProductColorService {
    List<ProductColorResponsePayload> getAllColors();

    ProductColorResponsePayload getColorById(UUID id);

    ProductColorResponsePayload createColor(ProductColorRequestPayload requestPayload);

    ProductColorResponsePayload updateColor(UUID id, ProductColorRequestPayload requestPayload);

    void deleteColor(UUID id);
}
