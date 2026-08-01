package com.ecommerce.backend.security.jwt.dtos;

import com.ecommerce.backend.security.jwt.enums.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.UUID;

@Builder
public record UserTokenPayload(
        @NotBlank UUID userId,
        @NotBlank String email,
        @NotBlank Role role
) {}
