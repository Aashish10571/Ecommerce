package com.ecommerce.backend.common.handler.category;

import com.ecommerce.backend.category.exception.CategoryAlreadyExistsException;
import com.ecommerce.backend.category.exception.CategoryNotFoundException;
import com.ecommerce.backend.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CategoryExceptionHandler {

    @ExceptionHandler(CategoryAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleCategoryAlreadyExists(
            CategoryAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(
                        "Category already exists",
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleCategoryNotFound(
            CategoryNotFoundException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(
                        "Category not found",
                        request.getRequestURI()
                ));
    }
}
