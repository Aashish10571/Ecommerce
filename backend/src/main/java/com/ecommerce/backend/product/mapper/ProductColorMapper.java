package com.ecommerce.backend.product.mapper;

import com.ecommerce.backend.product.dto.common.ProductColorPayload;
import com.ecommerce.backend.product.entity.ProductColor;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductColorMapper {

    ProductColorPayload toDto(ProductColor entity);

    List<ProductColorPayload> toDto(List<ProductColor> entities);
}
