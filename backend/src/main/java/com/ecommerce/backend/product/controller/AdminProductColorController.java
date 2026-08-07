package com.ecommerce.backend.product.controller;

import com.ecommerce.backend.common.dto.ApiResponse;
import com.ecommerce.backend.product.dto.request.ProductColorRequestPayload;
import com.ecommerce.backend.product.dto.response.ProductColorResponsePayload;
import com.ecommerce.backend.product.service.ProductColorService;
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
@RequestMapping("/api/v1/admin/color")
public class AdminProductColorController {

    private final ProductColorService productColorService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductColorResponsePayload>>> getColors(
            HttpServletRequest request
    ) {
        List<ProductColorResponsePayload> responsePayload = productColorService.getAllColors();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Colors fetched successfully",
                        responsePayload,
                        request.getRequestURI()
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductColorResponsePayload>> getColor(
            @Valid @PathVariable UUID id,
            HttpServletRequest request
    ) {
        ProductColorResponsePayload responsePayload = productColorService.getColorById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Color fetched successfully",
                        responsePayload,
                        request.getRequestURI()
                )
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductColorResponsePayload>> addColor(
            @Valid @RequestBody ProductColorRequestPayload requestPayload,
            HttpServletRequest request
    ) {
        ProductColorResponsePayload responsePayload = productColorService.createColor(requestPayload);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        "Color added successfully",
                        responsePayload,
                        request.getRequestURI()
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductColorResponsePayload>> updateColor(
            @PathVariable UUID id,
            @Valid @RequestBody ProductColorRequestPayload requestPayload,
            HttpServletRequest request
    ) {
        ProductColorResponsePayload responsePayload = productColorService.updateColor(id, requestPayload);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Updated color successfully",
                        responsePayload,
                        request.getRequestURI()
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteColor(
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        productColorService.deleteColor(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Color deleted successfully",
                        request.getRequestURI()
                )
        );
    }
}
