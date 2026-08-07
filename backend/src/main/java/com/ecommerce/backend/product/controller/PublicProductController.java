package com.ecommerce.backend.product.controller;

import com.ecommerce.backend.common.dto.ApiResponse;
import com.ecommerce.backend.product.dto.request.ProductFilterRequestPayload;
import com.ecommerce.backend.product.dto.response.ProductResponsePayload;
import com.ecommerce.backend.product.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/product")
public class PublicProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponsePayload>>> getActiveProducts(
            Pageable pageable,
            HttpServletRequest request
    ) {
        Page<ProductResponsePayload> responsePayload = productService.getAllActiveProducts(pageable);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Products fetched successfully",
                        responsePayload,
                        request.getRequestURI()
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductResponsePayload>>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID sizeId,
            @RequestParam(required = false) UUID colorId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStockOnly,
            Pageable pageable,
            HttpServletRequest request
    ) {
        ProductFilterRequestPayload filter = new ProductFilterRequestPayload(
                keyword, categoryId, sizeId, colorId, minPrice, maxPrice, inStockOnly
        );
        Page<ProductResponsePayload> responsePayload = productService.searchProducts(filter, pageable);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Products fetched successfully",
                        responsePayload,
                        request.getRequestURI()
                )
        );
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<ProductResponsePayload>> getProduct(
            @Valid @PathVariable String slug,
            HttpServletRequest request
    ) {
        ProductResponsePayload responsePayload = productService.getProductsBySlug(slug);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Product fetched successfully",
                        responsePayload,
                        request.getRequestURI()
                )
        );
    }
}
