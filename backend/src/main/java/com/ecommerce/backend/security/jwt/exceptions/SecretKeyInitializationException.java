package com.ecommerce.backend.security.jwt.exceptions;

public class SecretKeyInitializationException extends RuntimeException {

    public SecretKeyInitializationException(String message) {
        super(message);
    }

    public SecretKeyInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
