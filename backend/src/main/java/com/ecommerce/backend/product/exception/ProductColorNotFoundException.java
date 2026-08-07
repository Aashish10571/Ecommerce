package com.ecommerce.backend.product.exception;

public class ProductColorNotFoundException extends RuntimeException {

    public ProductColorNotFoundException(String message) {
        super(message);
    }

    public ProductColorNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
