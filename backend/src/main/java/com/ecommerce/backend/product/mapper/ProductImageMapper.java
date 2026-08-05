package com.ecommerce.backend.product.mapper;

import com.ecommerce.backend.product.dto.common.ProductImagePayload;
import com.ecommerce.backend.product.entity.ProductImage;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductImageMapper {

    ProductImagePayload toDto(ProductImage entity);

    List<ProductImagePayload> toDto(List<ProductImage> entities);
}
