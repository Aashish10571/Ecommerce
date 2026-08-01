package com.ecommerce.backend.common.handler.jwt;

import com.ecommerce.backend.common.dto.ApiResponse;
import com.ecommerce.backend.security.jwt.exceptions.SecretKeyInitializationException;
import com.ecommerce.backend.security.jwt.exceptions.TokenExpiredException;
import com.ecommerce.backend.security.jwt.exceptions.TokenInvalidException;
import com.ecommerce.backend.security.jwt.exceptions.TokenMissingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class JwtExceptionHandler {

    @ExceptionHandler(SecretKeyInitializationException.class)
    public ResponseEntity<ApiResponse<Object>> handleSecretKeyInitialization(
            SecretKeyInitializationException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        "Failed to initialize secret key",
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ApiResponse<Object>> handleTokenExpired(
            TokenExpiredException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(
                        "Authentication token has expired",
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(TokenInvalidException.class)
    public ResponseEntity<ApiResponse<Object>> handleTokenInvalid(
            TokenInvalidException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(
                        "Authentication token is invalid",
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(TokenMissingException.class)
    public ResponseEntity<ApiResponse<Object>> handleTokenMissing(
            TokenMissingException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(
                        "Authentication token is missing",
                        request.getRequestURI()
                ));
    }
}
