package com.ecommerce.backend.product.mapper;

import com.ecommerce.backend.product.dto.response.ProductVariantResponsePayload;
import com.ecommerce.backend.product.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {
                ProductSizeMapper.class,
                ProductColorMapper.class
        }
)
public interface ProductVariantMapper {

    @Mapping(target = "available", expression = "java(entity.getStock() > 0)")
    ProductVariantResponsePayload toDto(ProductVariant entity);

    List<ProductVariantResponsePayload> toDto(List<ProductVariant> entities);
}
