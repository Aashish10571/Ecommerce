package com.ecommerce.backend.category.service.impl;

import com.ecommerce.backend.category.dto.request.CategoryRequestPayload;
import com.ecommerce.backend.category.dto.response.CategoryResponsePayload;
import com.ecommerce.backend.category.entity.Category;
import com.ecommerce.backend.category.exception.CategoryAlreadyExistsException;
import com.ecommerce.backend.category.exception.CategoryNotFoundException;
import com.ecommerce.backend.category.mapper.CategoryMapper;
import com.ecommerce.backend.category.repository.CategoryRepository;
import com.ecommerce.backend.category.service.CategoryService;
import com.ecommerce.backend.common.util.SlugUtil;
import com.ecommerce.backend.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final String UNCATEGORIZED_SLUG = "uncategorized";

    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final SlugUtil slugUtil;

    @Override
    public List<CategoryResponsePayload> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categoryMapper.toDto(categories);
    }

    @Override
    public CategoryResponsePayload getCategoryBySlug(String slug) {

        Category category = categoryRepository.findBySlug(slug).orElseThrow(() -> new CategoryNotFoundException("Category not found."));

        return categoryMapper.toDto(category);
    }

    @Override
    public CategoryResponsePayload addNewCategory(CategoryRequestPayload requestPayload) {
        String slug = slugUtil.generate(requestPayload.name());

        validateCategory(
                requestPayload.name(),
                slug,
                null
        );

        Category category = categoryMapper.toEntity(requestPayload);
        category.setSlug(slug);

        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toDto(savedCategory);
    }

    @Override
    public CategoryResponsePayload updateCategory(
            UUID id,
            CategoryRequestPayload requestPayload
    ) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new CategoryNotFoundException("Category not found."));

        String slug = slugUtil.generate(requestPayload.name());

        validateCategory(
                requestPayload.name(),
                slug,
                id
        );

        category.setName(requestPayload.name());
        category.setSlug(slug);

        Category updatedCategory = categoryRepository.save(category);

        return categoryMapper.toDto(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new CategoryNotFoundException("Category not found."));

        if (category.getSlug().equals(UNCATEGORIZED_SLUG)) {
            throw new IllegalStateException("The uncategorized category cannot be deleted.");
        }

        Category uncategorized = categoryRepository.findBySlug(UNCATEGORIZED_SLUG)
                .orElseThrow(() -> new CategoryNotFoundException("Uncategorized category not found."));

        productRepository.reassignCategory(category, uncategorized);

        categoryRepository.delete(category);
    }

    private void validateCategory(
            String name,
            String slug,
            UUID categoryId
    ) {

        categoryRepository.findByName(name)
                .filter(existing ->
                        categoryId == null ||
                                !existing.getId().equals(categoryId))
                .ifPresent(existing -> {
                    throw new CategoryAlreadyExistsException(
                            "Category name already exists."
                    );
                });

        categoryRepository.findBySlug(slug)
                .filter(existing ->
                        categoryId == null ||
                                !existing.getId().equals(categoryId))
                .ifPresent(existing -> {
                    throw new CategoryAlreadyExistsException(
                            "Category slug already exists."
                    );
                });
    }
}