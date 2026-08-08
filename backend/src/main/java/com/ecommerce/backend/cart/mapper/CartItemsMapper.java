package com.ecommerce.backend.cart.mapper;

import com.ecommerce.backend.cart.dto.response.CartItemsResponsePayload;
import com.ecommerce.backend.cart.entity.CartItems;
import com.ecommerce.backend.product.mapper.ProductVariantMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = { ProductVariantMapper.class }
)
public interface CartItemsMapper {

    CartItemsResponsePayload toDto(CartItems entity);

    List<CartItemsResponsePayload> toDto(List<CartItems> entities);
}
