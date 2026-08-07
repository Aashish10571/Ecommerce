package com.ecommerce.backend.product.exception;

public class ProductSizeAlreadyExistsException extends RuntimeException {

    public ProductSizeAlreadyExistsException(String message) {
        super(message);
    }

    public ProductSizeAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
