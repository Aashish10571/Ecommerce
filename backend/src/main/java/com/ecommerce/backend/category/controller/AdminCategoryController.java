package com.ecommerce.backend.category.controller;

import com.ecommerce.backend.category.dto.request.CategoryRequestPayload;
import com.ecommerce.backend.category.dto.response.CategoryResponsePayload;
import com.ecommerce.backend.category.service.CategoryService;
import com.ecommerce.backend.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/category")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponsePayload>> addCategory (
            @Valid @RequestBody CategoryRequestPayload requestPayload,
            HttpServletRequest request
    ) {
        CategoryResponsePayload responsePayload = categoryService.addNewCategory(requestPayload);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        "Category added successfully",
                        responsePayload,
                        request.getRequestURI()
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponsePayload>> updateCategory (
            @PathVariable UUID id,
            @Valid @RequestBody CategoryRequestPayload requestPayload,
            HttpServletRequest request
    ) {
        CategoryResponsePayload responsePayload = categoryService.updateCategory(id, requestPayload);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Updated category successfully",
                        responsePayload,
                        request.getRequestURI()
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteCategory(
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        categoryService.deleteCategory(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Category deleted successfully",
                        request.getRequestURI()
                )
        );
    }
}
