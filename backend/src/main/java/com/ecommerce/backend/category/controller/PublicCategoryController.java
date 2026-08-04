package com.ecommerce.backend.category.controller;

import com.ecommerce.backend.category.dto.response.CategoryResponsePayload;
import com.ecommerce.backend.category.service.CategoryService;
import com.ecommerce.backend.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/category")
public class PublicCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponsePayload>>> getCategories(
            HttpServletRequest request
    ) {
        List<CategoryResponsePayload> responsePayload = categoryService.getAllCategories();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Categories fetched successfully",
                        responsePayload,
                        request.getRequestURI()
                )
        );
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<CategoryResponsePayload>> getCategory(
            @Valid @PathVariable String slug,
            HttpServletRequest request
    ) {
        CategoryResponsePayload responsePayload = categoryService.getCategoryBySlug(slug);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Category fetched successfully",
                        responsePayload,
                        request.getRequestURI()
                )
        );
    }
}
