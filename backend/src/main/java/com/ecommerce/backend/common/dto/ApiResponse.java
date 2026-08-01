package com.ecommerce.backend.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        String status,
        String message,
        T data,
        String requestUri,
        Instant timeStamp
) {

    public static <T> ApiResponse<T> success(
            String message,
            T data,
            String requestUri
    ) {
        return new ApiResponse<>(
                "SUCCESS",
                message,
                data,
                requestUri,
                Instant.now()
        );
    }

    public static <T> ApiResponse<T> success(
            String message,
            String requestUri
    ) {
        return new ApiResponse<>(
                "SUCCESS",
                message,
                null,
                requestUri,
                Instant.now()
        );
    }

    public static <T> ApiResponse<T> error(
            String message,
            T data,
            String requestUri
    ) {
        return new ApiResponse<>(
                "ERROR",
                message,
                data,
                requestUri,
                Instant.now()
        );
    }

    public static <T> ApiResponse<T> error(
            String message,
            String requestUri
    ) {
        return new ApiResponse<>(
                "ERROR",
                message,
                null,
                requestUri,
                Instant.now()
        );
    }
}