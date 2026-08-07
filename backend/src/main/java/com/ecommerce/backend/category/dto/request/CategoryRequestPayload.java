package com.ecommerce.backend.category.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryRequestPayload(
        @NotBlank(message = "Category name is required")
        @Size(
                min = 2,
                max = 100,
                message = "Category name must be between 2 and 100 characters"
        )
        @Pattern(
                regexp = "^[a-zA-Z0-9\\s&'-]+$",
                message = "Category name cannot contain special characters"
        )
        String name
) { }
