package com.ecommerce.backend.cart.service;

import com.ecommerce.backend.cart.dto.request.AddCartItemsPayload;
import com.ecommerce.backend.cart.dto.request.UpdateCartItemsPayload;
import com.ecommerce.backend.cart.dto.response.CartItemsResponsePayload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface CartService {
    @Transactional(readOnly = true)
    Page<CartItemsResponsePayload> getCartItems(UUID userId, Pageable pageable);

    CartItemsResponsePayload addItemToCart(UUID userId, AddCartItemsPayload requestPayload);

    @Transactional
    CartItemsResponsePayload updateCartItemQuantity(UUID userId, UUID itemId, UpdateCartItemsPayload request);

    @Transactional
    void removeCartItem(UUID userId, UUID itemId);
}
