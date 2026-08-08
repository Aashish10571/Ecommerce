package com.ecommerce.backend.cart.mapper;

import com.ecommerce.backend.cart.dto.response.CartResponsePayload;
import com.ecommerce.backend.cart.entity.Cart;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring",
        uses = { CartItemsMapper.class }
)
public interface CartMapper {

    CartResponsePayload toDto(Cart entity);
}
