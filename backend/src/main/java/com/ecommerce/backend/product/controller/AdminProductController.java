package com.ecommerce.backend.product.controller;

import com.ecommerce.backend.common.dto.ApiResponse;
import com.ecommerce.backend.product.dto.request.ProductRequestPayload;
import com.ecommerce.backend.product.dto.response.ProductResponsePayload;
import com.ecommerce.backend.product.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/product")
public class AdminProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponsePayload>>> getProducts(
            Pageable pageable,
            HttpServletRequest request
    ) {
        Page<ProductResponsePayload> responsePayload = productService.getAllProducts(pageable);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Products fetched successfully",
                        responsePayload,
                        request.getRequestURI()
                )
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponsePayload>> addProduct(
            @Valid @RequestBody ProductRequestPayload requestPayload,
            HttpServletRequest request
    ) {
        ProductResponsePayload responsePayload = productService.createProduct(requestPayload);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        "Product added successfully",
                        responsePayload,
                        request.getRequestURI()
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponsePayload>> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequestPayload requestPayload,
            HttpServletRequest request
    ) {
        ProductResponsePayload responsePayload = productService.updateProduct(id, requestPayload);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Updated product successfully",
                        responsePayload,
                        request.getRequestURI()
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteProduct(
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        productService.deleteProduct(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Product deleted successfully",
                        request.getRequestURI()
                )
        );
    }
}
