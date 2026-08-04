package com.ecommerce.backend.category.mapper;

import com.ecommerce.backend.category.dto.request.CategoryRequestPayload;
import com.ecommerce.backend.category.dto.response.CategoryResponsePayload;
import com.ecommerce.backend.category.entity.Category;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoryMapper {

    public CategoryResponsePayload toDto(Category category) {
        return new CategoryResponsePayload(
                category.getId(),
                category.getName(),
                category.getSlug()
        );
    }

    public List<CategoryResponsePayload> toDto(List<Category> categories) {
        return categories.stream()
                .map(this::toDto)
                .toList();
    }

    public Category toEntity(CategoryRequestPayload payload) {
        return Category.builder()
                .name(payload.name())
                .slug(payload.slug())
                .build();
    }
}
