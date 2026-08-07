package com.ecommerce.backend.product.mapper;

import com.ecommerce.backend.category.mapper.CategoryMapper;
import com.ecommerce.backend.product.dto.response.ProductResponsePayload;
import com.ecommerce.backend.product.entity.Product;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {
                CategoryMapper.class,
                ProductImageMapper.class,
                ProductVariantMapper.class
        }
)
public interface ProductMapper {

        ProductResponsePayload toDto(Product entity);

        List<ProductResponsePayload> toDto(List<Product> entities);
}
