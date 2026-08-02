package com.ecommerce.backend.profile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsernameChangePayload(

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
        @Pattern(
                regexp = "^[a-zA-Z][a-zA-Z0-9_]*$",
                message = "Username must start with a letter and contain only letters, numbers, or underscores"
        )
        String newUsername
) { }
