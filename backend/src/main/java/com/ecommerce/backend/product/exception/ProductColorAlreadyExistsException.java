package com.ecommerce.backend.product.exception;

public class ProductColorAlreadyExistsException extends RuntimeException {

    public ProductColorAlreadyExistsException(String message) {
        super(message);
    }

    public ProductColorAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
