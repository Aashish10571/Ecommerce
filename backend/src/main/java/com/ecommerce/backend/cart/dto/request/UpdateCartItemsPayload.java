package com.ecommerce.backend.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartItemsPayload (
        @NotNull @Min(1) Integer quantity
) { }
