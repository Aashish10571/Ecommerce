package com.ecommerce.backend.auth.exception;

public class AuthProviderException extends RuntimeException {

    public AuthProviderException(String message) {
        super(message);
    }

    public AuthProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
