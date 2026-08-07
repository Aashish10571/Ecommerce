package com.ecommerce.backend.product.exception;

public class ProductSizeNotFoundException extends RuntimeException {

    public ProductSizeNotFoundException(String message) {
        super(message);
    }

    public ProductSizeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
