package com.ecommerce.backend.category.service;

import com.ecommerce.backend.category.dto.request.CategoryRequestPayload;
import com.ecommerce.backend.category.dto.response.CategoryResponsePayload;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    List<CategoryResponsePayload> getAllCategories();

    CategoryResponsePayload getCategoryBySlug(String slug);

    CategoryResponsePayload addNewCategory(CategoryRequestPayload payload);

    CategoryResponsePayload updateCategory(UUID id, CategoryRequestPayload requestPayload);

    void deleteCategory(UUID id);
}
