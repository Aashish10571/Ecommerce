package com.ecommerce.backend.category.mapper;

import com.ecommerce.backend.category.dto.request.CategoryRequestPayload;
import com.ecommerce.backend.category.dto.response.CategoryResponsePayload;
import com.ecommerce.backend.category.entity.Category;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponsePayload toDto(Category category);

    List<CategoryResponsePayload> toDto(List<Category> categories);

    Category toEntity(CategoryRequestPayload payload);

}