package com.ecommerce.backend.product.mapper;

import com.ecommerce.backend.product.dto.request.ProductSizeRequestPayload;
import com.ecommerce.backend.product.dto.response.ProductSizeResponsePayload;
import com.ecommerce.backend.product.entity.ProductSize;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductSizeMapper {

    ProductSizeResponsePayload toDto(ProductSize entity);

    List<ProductSizeResponsePayload> toDto(List<ProductSize> entities);

    ProductSize toEntity(ProductSizeRequestPayload requestPayload);
}
