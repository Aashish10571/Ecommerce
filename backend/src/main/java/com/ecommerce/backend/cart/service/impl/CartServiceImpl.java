package com.ecommerce.backend.cart.service.impl;

import com.ecommerce.backend.auth.entity.User;
import com.ecommerce.backend.auth.repository.UserRepository;
import com.ecommerce.backend.cart.dto.request.AddCartItemsPayload;
import com.ecommerce.backend.cart.dto.request.UpdateCartItemsPayload;
import com.ecommerce.backend.cart.dto.response.CartItemsResponsePayload;
import com.ecommerce.backend.cart.entity.Cart;
import com.ecommerce.backend.cart.entity.CartItems;
import com.ecommerce.backend.cart.exception.CartItemNotFoundException;
import com.ecommerce.backend.cart.mapper.CartItemsMapper;
import com.ecommerce.backend.cart.repository.CartItemsRepository;
import com.ecommerce.backend.cart.repository.CartRepository;
import com.ecommerce.backend.cart.service.CartService;
import com.ecommerce.backend.product.entity.ProductVariant;
import com.ecommerce.backend.product.exception.ProductNotFoundException;
import com.ecommerce.backend.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemsMapper cartItemsMapper;
    private final CartItemsRepository cartItemsRepository;
    private final ProductVariantRepository productVariantRepository;

    @Transactional(readOnly = true)
    @Override
    public Page<CartItemsResponsePayload> getCartItems(UUID userId, Pageable pageable) {
        return cartItemsRepository.findByCart_User_Id(userId, pageable).map(cartItemsMapper::toDto);
    }

    @Override
    public CartItemsResponsePayload addItemToCart(UUID userId, AddCartItemsPayload requestPayload) {
        Cart cart = cartRepository.findByUser_Id(userId).orElseGet(() -> createCartForUser(userId));

        ProductVariant variant = productVariantRepository.findById(requestPayload.variantId()).orElseThrow(() -> new ProductNotFoundException("Product variant not found"));

        CartItems item = cartItemsRepository.findByCart_IdAndVariant_Id(cart.getId(), variant.getId())
                .map(existing -> {
                    existing.setQuantity(existing.getQuantity() + requestPayload.quantity());
                    return existing;
                })
                .orElseGet(() -> CartItems
                        .builder()
                        .cart(cart)
                        .variant(variant)
                        .quantity(requestPayload.quantity())
                        .build()
                );

        CartItems savedItem = cartItemsRepository.save(item);

        return cartItemsMapper.toDto(savedItem);
    }

    @Transactional
    @Override
    public CartItemsResponsePayload updateCartItemQuantity(UUID userId, UUID itemId, UpdateCartItemsPayload request) {
        CartItems item = cartItemsRepository.findByIdAndCart_User_Id(itemId, userId).orElseThrow(() -> new CartItemNotFoundException("Cart item not found"));
        item.setQuantity(request.quantity());

        CartItems savedItem = cartItemsRepository.save(item);
        return cartItemsMapper.toDto(savedItem);
    }

    @Transactional
    @Override
    public void removeCartItem(UUID userId, UUID itemId) {
        CartItems item = cartItemsRepository.findByIdAndCart_User_Id(itemId, userId).orElseThrow(() -> new CartItemNotFoundException("Cart item not found"));

        cartItemsRepository.delete(item);
    }

    private Cart createCartForUser(UUID userId) {
        User user = userRepository.getReferenceById(userId);
        Cart cart = Cart.builder().user(user).build();

        return cartRepository.save(cart);
    }
}
