package com.ecommerce.backend.auth.exception;

public class GoogleTokenInvalidException extends RuntimeException {

    public GoogleTokenInvalidException(String message) {
        super(message);
    }

    public GoogleTokenInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}
