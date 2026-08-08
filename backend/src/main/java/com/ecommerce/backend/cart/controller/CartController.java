package com.ecommerce.backend.cart.controller;

import com.ecommerce.backend.cart.dto.request.AddCartItemsPayload;
import com.ecommerce.backend.cart.dto.request.UpdateCartItemsPayload;
import com.ecommerce.backend.cart.dto.response.CartItemsResponsePayload;
import com.ecommerce.backend.cart.service.CartService;
import com.ecommerce.backend.common.dto.ApiResponse;
import com.ecommerce.backend.security.jwt.dtos.UserTokenPayload;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cart/items")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CartItemsResponsePayload>>> getCartItems(
            @AuthenticationPrincipal UserTokenPayload principal,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable,
            HttpServletRequest request
    ) {
        Page<CartItemsResponsePayload> responsePayload = cartService.getCartItems(principal.userId(), pageable);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cart items fetched successfully",
                        responsePayload,
                        request.getRequestURI()
                )
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CartItemsResponsePayload>> addItemToCart(
            @AuthenticationPrincipal UserTokenPayload principal,
            @Valid @RequestBody AddCartItemsPayload requestPayload,
            HttpServletRequest request
    ) {
        CartItemsResponsePayload responsePayload = cartService.addItemToCart(principal.userId(), requestPayload);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        "Item added to cart successfully",
                        responsePayload,
                        request.getRequestURI()
                )
        );
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ApiResponse<CartItemsResponsePayload>> updateCartItem(
            @AuthenticationPrincipal UserTokenPayload principal,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemsPayload requestPayload,
            HttpServletRequest request
    ) {
        CartItemsResponsePayload responsePayload = cartService.updateCartItemQuantity(principal.userId(), itemId, requestPayload);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cart item updated successfully",
                        responsePayload,
                        request.getRequestURI()
                )
        );
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Object>> deleteCartItem(
            @AuthenticationPrincipal UserTokenPayload principal,
            @PathVariable UUID itemId,
            HttpServletRequest request
    ) {
        cartService.removeCartItem(principal.userId(), itemId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cart item removed successfully",
                        request.getRequestURI()
                )
        );
    }
}