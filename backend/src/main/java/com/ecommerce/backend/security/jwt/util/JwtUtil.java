package com.ecommerce.backend.security.jwt.util;

import com.ecommerce.backend.security.jwt.dtos.UserTokenPayload;
import com.ecommerce.backend.security.jwt.enums.Role;
import com.ecommerce.backend.security.jwt.enums.Token;
import com.ecommerce.backend.security.jwt.exceptions.SecretKeyInitializationException;
import com.ecommerce.backend.security.jwt.exceptions.TokenExpiredException;
import com.ecommerce.backend.security.jwt.exceptions.TokenInvalidException;
import com.ecommerce.backend.security.jwt.exceptions.TokenMissingException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.token.access.expire.time}")
    private Long accessTokenExpirationTime;

    @Value("${jwt.token.refresh.expire.time}")
    private Long refreshTokenExpirationTime;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new SecretKeyInitializationException("Secret key configured incorrectly");
        }

        try {
            secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new SecretKeyInitializationException("Failed to initialize secret key", exception);
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Optional<String> extractToken(HttpServletRequest request) {
        return TokenExtractor.extractTokenFromHeader(request);
    }

    public String generateToken(Token tokenType, UserTokenPayload tokenPayload, UUID tokenId) {
        long expirationTime = tokenType == Token.ACCESS_TOKEN ? accessTokenExpirationTime : refreshTokenExpirationTime;

        JwtBuilder jwtBuilder = Jwts.builder()
                .id(tokenId.toString())
                .subject(tokenPayload.email())
                .claim("userId", tokenPayload.userId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime));

        if (tokenType == Token.ACCESS_TOKEN) {
            jwtBuilder.claim("role", tokenPayload.role().name());
        }

        return jwtBuilder.signWith(secretKey).compact();
    }

    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            throw new TokenMissingException("Authentication token is missing");
        }

        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException exception) {
            throw new TokenExpiredException("Authentication token has expired", exception);
        } catch (UnsupportedJwtException exception) {
            throw new TokenInvalidException("Unsupported authentication token", exception);
        } catch (MalformedJwtException exception) {
            throw new TokenInvalidException("Invalid authentication token format", exception);
        } catch (SecurityException exception) {
            throw new TokenInvalidException("Authentication token signature is invalid", exception);
        } catch (IllegalArgumentException exception) {
            throw new TokenInvalidException("Authentication token data is invalid", exception);
        } catch (JwtException exception) {
            throw new TokenInvalidException("Authentication token is invalid", exception);
        }
    }

    public UserTokenPayload extractUserPayload(String token) {
        Claims claims = parseClaims(token);

        String email = claims.getSubject();
        String userIdStr = claims.get("userId", String.class);
        String roleStr = claims.get("role", String.class);

        if (email == null || email.isBlank() || userIdStr == null || userIdStr.isBlank()) {
            throw new TokenInvalidException("Invalid authentication token payload structure");
        }

        UUID userId;
        try {
            userId = UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            throw new TokenInvalidException("Invalid user ID format in token");
        }

        Role role = roleStr != null ? Role.valueOf(roleStr) : Role.USER;

        return new UserTokenPayload(userId, email, role);
    }

    public UUID extractTokenId(String token) {
        Claims claims = parseClaims(token);
        String tokenId = claims.getId();

        if (tokenId == null || tokenId.isBlank()) {
            throw new TokenInvalidException("Token is missing an identifier");
        }

        try {
            return UUID.fromString(tokenId);
        } catch (IllegalArgumentException e) {
            throw new TokenInvalidException("Invalid token identifier format");
        }
    }
}
