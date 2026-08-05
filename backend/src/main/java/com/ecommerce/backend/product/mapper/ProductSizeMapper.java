package com.ecommerce.backend.product.mapper;

import com.ecommerce.backend.product.dto.common.ProductSizePayload;
import com.ecommerce.backend.product.entity.ProductSize;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductSizeMapper {

    ProductSizePayload toDto(ProductSize entity);

    List<ProductSizePayload> toDto(List<ProductSize> entities);
}
