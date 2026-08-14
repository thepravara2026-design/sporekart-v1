package com.sporekart.application.exception;

import com.sporekart.modules.catalog.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NoHandlerFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {} {}", request.getMethod(), request.getRequestURI());
        ApiErrorResponse response = ApiErrorResponse.of("NOT_FOUND", "Requested resource was not found", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleProductNotFound(ProductNotFoundException ex, HttpServletRequest request) {
        log.warn("Product not found on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("CATALOG_PRODUCT_NOT_FOUND", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleCategoryNotFound(CategoryNotFoundException ex, HttpServletRequest request) {
        log.warn("Category not found on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("CATALOG_CATEGORY_NOT_FOUND", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler({DuplicateSkuException.class, DuplicateCategoryException.class})
    public ResponseEntity<ApiErrorResponse> handleDuplicateResource(RuntimeException ex, HttpServletRequest request) {
        log.warn("Duplicate resource conflict on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("CONFLICT", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler({InvalidProductStateException.class, CategoryDeletionException.class})
    public ResponseEntity<ApiErrorResponse> handleDomainStateConflict(RuntimeException ex, HttpServletRequest request) {
        log.warn("Domain state conflict on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("BAD_REQUEST", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String validationMessage = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("Validation failed");
        log.warn("Validation error on {}: {}", request.getRequestURI(), validationMessage);
        ApiErrorResponse response = ApiErrorResponse.of("VALIDATION_ERROR", validationMessage, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedRequest(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed HTTP request on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("MALFORMED_REQUEST", "Malformed JSON request payload", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("HTTP method not supported on {}: {}", request.getRequestURI(), ex.getMethod());
        ApiErrorResponse response = ApiErrorResponse.of("METHOD_NOT_ALLOWED", "HTTP method " + ex.getMethod() + " is not supported for this endpoint", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Invalid argument on {}: {}", request.getRequestURI(), ex.getMessage());
        String code = "BAD_REQUEST";
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("sort field") || ex.getMessage().contains("sort direction")) {
                code = "CATALOG_INVALID_SORT";
            } else if (ex.getMessage().contains("Page size") || ex.getMessage().contains("Page index")) {
                code = "CATALOG_INVALID_PAGE_SIZE";
            } else if (ex.getMessage().contains("price") || ex.getMessage().contains("Price")) {
                code = "CATALOG_INVALID_PRICE_RANGE";
            }
        }
        ApiErrorResponse response = ApiErrorResponse.of(code, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {}: ", request.getRequestURI(), ex);
        ApiErrorResponse response = ApiErrorResponse.of("INTERNAL_SERVER_ERROR", "An unexpected error occurred. Please try again later.", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
