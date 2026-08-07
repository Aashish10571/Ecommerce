package com.ecommerce.backend.product.controller;

import com.ecommerce.backend.common.dto.ApiResponse;
import com.ecommerce.backend.product.dto.request.ProductSizeRequestPayload;
import com.ecommerce.backend.product.dto.response.ProductSizeResponsePayload;
import com.ecommerce.backend.product.service.ProductSizeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/size")
public class AdminProductSizeController {

    private final ProductSizeService productSizeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductSizeResponsePayload>>> getSizes(
            HttpServletRequest request
    ) {
        List<ProductSizeResponsePayload> responsePayload = productSizeService.getAllSizes();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Sizes fetched successfully",
                        responsePayload,
                        request.getRequestURI()
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductSizeResponsePayload>> getSize(
            @Valid @PathVariable UUID id,
            HttpServletRequest request
    ) {
        ProductSizeResponsePayload responsePayload = productSizeService.getSizeById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Size fetched successfully",
                        responsePayload,
                        request.getRequestURI()
                )
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductSizeResponsePayload>> addSize(
            @Valid @RequestBody ProductSizeRequestPayload requestPayload,
            HttpServletRequest request
    ) {
        ProductSizeResponsePayload responsePayload = productSizeService.createSize(requestPayload);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        "Size added successfully",
                        responsePayload,
                        request.getRequestURI()
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductSizeResponsePayload>> updateSize(
            @PathVariable UUID id,
            @Valid @RequestBody ProductSizeRequestPayload requestPayload,
            HttpServletRequest request
    ) {
        ProductSizeResponsePayload responsePayload = productSizeService.updateSize(id, requestPayload);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Updated size successfully",
                        responsePayload,
                        request.getRequestURI()
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteSize(
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        productSizeService.deleteSize(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Size deleted successfully",
                        request.getRequestURI()
                )
        );
    }
}
