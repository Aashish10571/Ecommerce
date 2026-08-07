package com.ecommerce.backend.product.mapper;

import com.ecommerce.backend.product.dto.request.ProductColorRequestPayload;
import com.ecommerce.backend.product.dto.response.ProductColorResponsePayload;
import com.ecommerce.backend.product.entity.ProductColor;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductColorMapper {

    ProductColorResponsePayload toDto(ProductColor entity);

    List<ProductColorResponsePayload> toDto(List<ProductColor> entities);

    ProductColor toEntity(ProductColorRequestPayload requestPayload);
}
