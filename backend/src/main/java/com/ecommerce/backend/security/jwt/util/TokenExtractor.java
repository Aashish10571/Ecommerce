package com.ecommerce.backend.security.jwt.util;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

public class TokenExtractor {

    public static Optional<String> extractTokenFromHeader(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            return Optional.empty();
        }
        String token = header.substring(7);

        return token.isBlank() ? Optional.empty() : Optional.of(token);
    }
}
