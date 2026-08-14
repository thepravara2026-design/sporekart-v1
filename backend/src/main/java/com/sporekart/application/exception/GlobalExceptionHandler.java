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

    @ExceptionHandler(com.sporekart.modules.cart.domain.exception.CartNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleCartNotFound(com.sporekart.modules.cart.domain.exception.CartNotFoundException ex, HttpServletRequest request) {
        log.warn("Cart not found on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("CART_NOT_FOUND", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.cart.domain.exception.CartItemNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleCartItemNotFound(com.sporekart.modules.cart.domain.exception.CartItemNotFoundException ex, HttpServletRequest request) {
        log.warn("Cart item not found on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("CART_ITEM_NOT_FOUND", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.cart.domain.exception.CartNotModifiableException.class)
    public ResponseEntity<ApiErrorResponse> handleCartNotModifiable(com.sporekart.modules.cart.domain.exception.CartNotModifiableException ex, HttpServletRequest request) {
        log.warn("Cart not modifiable on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("CART_NOT_MODIFIABLE", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.cart.domain.exception.InvalidQuantityException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidQuantity(com.sporekart.modules.cart.domain.exception.InvalidQuantityException ex, HttpServletRequest request) {
        log.warn("Invalid quantity on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("CART_INVALID_QUANTITY", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.cart.domain.exception.ProductNotPurchasableException.class)
    public ResponseEntity<ApiErrorResponse> handleProductNotPurchasable(com.sporekart.modules.cart.domain.exception.ProductNotPurchasableException ex, HttpServletRequest request) {
        log.warn("Product not purchasable on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("CATALOG_PRODUCT_NOT_PURCHASABLE", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.cart.domain.exception.CartAccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleCartAccessDenied(com.sporekart.modules.cart.domain.exception.CartAccessDeniedException ex, HttpServletRequest request) {
        log.warn("Cart access denied on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("UNAUTHORIZED", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiErrorResponse> handleOptimisticLockingFailure(org.springframework.orm.ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {
        log.warn("Cart optimistic locking conflict on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("CART_CONCURRENCY_CONFLICT", "Cart was modified concurrently by another request. Please try again.", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.checkout.domain.exception.CartEmptyException.class)
    public ResponseEntity<ApiErrorResponse> handleCartEmpty(com.sporekart.modules.checkout.domain.exception.CartEmptyException ex, HttpServletRequest request) {
        log.warn("Cart empty for checkout preview on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("CHECKOUT_CART_EMPTY", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.checkout.domain.exception.CheckoutNotEligibleException.class)
    public ResponseEntity<ApiErrorResponse> handleCheckoutNotEligible(com.sporekart.modules.checkout.domain.exception.CheckoutNotEligibleException ex, HttpServletRequest request) {
        log.warn("Checkout not eligible on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("CHECKOUT_NOT_ELIGIBLE", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.checkout.domain.exception.CurrencyMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleCurrencyMismatch(com.sporekart.modules.checkout.domain.exception.CurrencyMismatchException ex, HttpServletRequest request) {
        log.warn("Currency mismatch on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("CHECKOUT_CURRENCY_MISMATCH", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.checkout.domain.exception.TaxCalculationException.class)
    public ResponseEntity<ApiErrorResponse> handleTaxCalculationFailed(com.sporekart.modules.checkout.domain.exception.TaxCalculationException ex, HttpServletRequest request) {
        log.error("Tax calculation error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        ApiErrorResponse response = ApiErrorResponse.of("CHECKOUT_TAX_CALCULATION_FAILED", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.checkout.domain.exception.ShippingRateUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleShippingRateUnavailable(com.sporekart.modules.checkout.domain.exception.ShippingRateUnavailableException ex, HttpServletRequest request) {
        log.warn("Shipping rate unavailable on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("CHECKOUT_SHIPPING_RATE_UNAVAILABLE", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.order.domain.exception.OrderNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleOrderNotFound(com.sporekart.modules.order.domain.exception.OrderNotFoundException ex, HttpServletRequest request) {
        log.warn("Order not found on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("ORDER_NOT_FOUND", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.order.domain.exception.OrderAccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleOrderAccessDenied(com.sporekart.modules.order.domain.exception.OrderAccessDeniedException ex, HttpServletRequest request) {
        log.warn("Order access denied on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("ORDER_ACCESS_DENIED", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.order.domain.exception.OrderNotCancellableException.class)
    public ResponseEntity<ApiErrorResponse> handleOrderNotCancellable(com.sporekart.modules.order.domain.exception.OrderNotCancellableException ex, HttpServletRequest request) {
        log.warn("Order not cancellable on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("ORDER_NOT_CANCELLABLE", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
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
