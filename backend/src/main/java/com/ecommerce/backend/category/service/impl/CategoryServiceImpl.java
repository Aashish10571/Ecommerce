package com.ecommerce.backend.category.service.impl;

import com.ecommerce.backend.category.dto.request.CategoryRequestPayload;
import com.ecommerce.backend.category.dto.response.CategoryResponsePayload;
import com.ecommerce.backend.category.entity.Category;
import com.ecommerce.backend.category.exception.CategoryAlreadyExistsException;
import com.ecommerce.backend.category.exception.CategoryNotFoundException;
import com.ecommerce.backend.category.mapper.CategoryMapper;
import com.ecommerce.backend.category.repository.CategoryRepository;
import com.ecommerce.backend.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryResponsePayload> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();

        return categoryMapper.toDto(categories);
    }

    @Override
    public CategoryResponsePayload getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug);

        return categoryMapper.toDto(category);
    }

    @Override
    public CategoryResponsePayload addNewCategory(CategoryRequestPayload payload) {

       verifyCategory(payload);

        Category category = categoryMapper.toEntity(payload);

        return categoryMapper.toDto(
                categoryRepository.save(category)
        );
    }

    @Override
    public CategoryResponsePayload updateCategory(UUID id, CategoryRequestPayload requestPayload) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        verifyCategory(requestPayload);

        category.setName(requestPayload.name());
        category.setSlug(requestPayload.slug());

        Category updatedCategory = categoryRepository.save(category);

        return categoryMapper.toDto(updatedCategory);
    }

    @Override
    public void deleteCategory(UUID id) {}

    private void verifyCategory(CategoryRequestPayload requestPayload) {
        if (categoryRepository.existsByName(requestPayload.name())) {
            throw new CategoryAlreadyExistsException(
                    "Category name already exists"
            );
        }

        if (categoryRepository.existsBySlug(requestPayload.slug())) {
            throw new CategoryAlreadyExistsException(
                    "Category slug already exists"
            );
        }
    }
}
