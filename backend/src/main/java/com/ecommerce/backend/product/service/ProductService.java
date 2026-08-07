package com.ecommerce.backend.product.service;

import com.ecommerce.backend.product.dto.request.ProductFilterRequestPayload;
import com.ecommerce.backend.product.dto.request.ProductRequestPayload;
import com.ecommerce.backend.product.dto.response.ProductResponsePayload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductService {
    Page<ProductResponsePayload> getAllProducts(Pageable pageable);

    Page<ProductResponsePayload> getAllActiveProducts(Pageable pageable);

    Page<ProductResponsePayload> searchProducts(ProductFilterRequestPayload filter, Pageable pageable);

    ProductResponsePayload getProductsBySlug(String slug);

    ProductResponsePayload createProduct(ProductRequestPayload requestPayload);

    ProductResponsePayload updateProduct(UUID id, ProductRequestPayload requestPayload);

    void deleteProduct(UUID id);
}
