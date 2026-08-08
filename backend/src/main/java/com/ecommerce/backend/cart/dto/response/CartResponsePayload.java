package com.ecommerce.backend.cart.dto.response;

import java.util.List;
import java.util.UUID;

public record CartResponsePayload(
        UUID id,
        List<CartItemsResponsePayload> items
) { }
