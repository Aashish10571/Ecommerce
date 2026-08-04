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
        String name,

        @NotBlank(message = "Slug is required")
        @Size(
                min = 2,
                max = 120,
                message = "Slug must be between 2 and 120 characters"
        )
        @Pattern(
                regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
                message = "Slug cannot contain uppercase letters, spaces, underscores, or special characters"
        )
        String slug
) { }
