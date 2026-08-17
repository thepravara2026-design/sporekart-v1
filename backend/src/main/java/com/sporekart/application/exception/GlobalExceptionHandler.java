package com.sporekart.application.exception;

import com.sporekart.modules.catalog.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final com.sporekart.application.observability.metrics.CommerceMetricsService metricsService;

    public GlobalExceptionHandler() {
        this(null);
    }

    public GlobalExceptionHandler(@org.springframework.beans.factory.annotation.Autowired(required = false) com.sporekart.application.observability.metrics.CommerceMetricsService metricsService) {
        this.metricsService = metricsService;
    }

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

    @ExceptionHandler(com.sporekart.modules.training.domain.exception.TrainingNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleTrainingNotFound(com.sporekart.modules.training.domain.exception.TrainingNotFoundException ex, HttpServletRequest request) {
        log.warn("Training program not found on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("TRAINING_NOT_FOUND", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.training.domain.exception.TrainingProgramAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleTrainingProgramAlreadyExists(com.sporekart.modules.training.domain.exception.TrainingProgramAlreadyExistsException ex, HttpServletRequest request) {
        log.warn("Training program conflict on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("TRAINING_PROGRAM_ALREADY_EXISTS", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.training.domain.exception.InvalidTrainingProgramDataException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidTrainingProgramData(com.sporekart.modules.training.domain.exception.InvalidTrainingProgramDataException ex, HttpServletRequest request) {
        log.warn("Invalid training program data on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("INVALID_TRAINING_PROGRAM_DATA", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.training.domain.exception.BatchAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleBatchAlreadyExists(com.sporekart.modules.training.domain.exception.BatchAlreadyExistsException ex, HttpServletRequest request) {
        log.warn("Batch conflict on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("BATCH_ALREADY_EXISTS", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.training.domain.exception.InvalidScheduleException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidSchedule(com.sporekart.modules.training.domain.exception.InvalidScheduleException ex, HttpServletRequest request) {
        log.warn("Invalid batch schedule on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("INVALID_SCHEDULE", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.training.domain.exception.TrainingProgramNotEligibleException.class)
    public ResponseEntity<ApiErrorResponse> handleTrainingProgramNotEligible(com.sporekart.modules.training.domain.exception.TrainingProgramNotEligibleException ex, HttpServletRequest request) {
        log.warn("Training program not eligible for batch creation on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("TRAINING_PROGRAM_NOT_ELIGIBLE", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.training.domain.exception.BatchReassignmentNotAllowedException.class)
    public ResponseEntity<ApiErrorResponse> handleBatchReassignmentNotAllowed(com.sporekart.modules.training.domain.exception.BatchReassignmentNotAllowedException ex, HttpServletRequest request) {
        log.warn("Batch reassignment prohibited on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("BATCH_REASSIGNMENT_NOT_ALLOWED", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.training.domain.exception.BatchNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleBatchNotFound(com.sporekart.modules.training.domain.exception.BatchNotFoundException ex, HttpServletRequest request) {
        log.warn("Training batch not found on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("BATCH_NOT_FOUND", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.training.domain.exception.BatchFullException.class)
    public ResponseEntity<ApiErrorResponse> handleBatchFull(com.sporekart.modules.training.domain.exception.BatchFullException ex, HttpServletRequest request) {
        log.warn("Training batch full on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("BATCH_FULL", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.training.domain.exception.EnrollmentNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEnrollmentNotFound(com.sporekart.modules.training.domain.exception.EnrollmentNotFoundException ex, HttpServletRequest request) {
        log.warn("Training enrollment not found on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("ENROLLMENT_NOT_FOUND", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.training.domain.exception.DuplicateEnrollmentException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateEnrollment(com.sporekart.modules.training.domain.exception.DuplicateEnrollmentException ex, HttpServletRequest request) {
        log.warn("Duplicate training enrollment on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("DUPLICATE_ENROLLMENT", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.training.domain.exception.InvalidEnrollmentStateException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidEnrollmentState(com.sporekart.modules.training.domain.exception.InvalidEnrollmentStateException ex, HttpServletRequest request) {
        log.warn("Invalid enrollment state on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("INVALID_ENROLLMENT_STATE", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.training.domain.exception.PaymentEnrollmentMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handlePaymentEnrollmentMismatch(com.sporekart.modules.training.domain.exception.PaymentEnrollmentMismatchException ex, HttpServletRequest request) {
        log.warn("Payment enrollment mismatch on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("PAYMENT_ENROLLMENT_MISMATCH", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.training.domain.exception.DuplicateDemandException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateDemand(com.sporekart.modules.training.domain.exception.DuplicateDemandException ex, HttpServletRequest request) {
        log.warn("Duplicate training demand on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("DUPLICATE_DEMAND", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.training.domain.exception.CancellationWindowExpiredException.class)
    public ResponseEntity<ApiErrorResponse> handleCancellationWindowExpired(com.sporekart.modules.training.domain.exception.CancellationWindowExpiredException ex, HttpServletRequest request) {
        log.warn("Cancellation window expired on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("CANCELLATION_WINDOW_EXPIRED", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.training.domain.exception.RescheduleWindowExpiredException.class)
    public ResponseEntity<ApiErrorResponse> handleRescheduleWindowExpired(com.sporekart.modules.training.domain.exception.RescheduleWindowExpiredException ex, HttpServletRequest request) {
        log.warn("Reschedule window expired on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("RESCHEDULE_WINDOW_EXPIRED", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.training.domain.exception.DemandNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleDemandNotFound(com.sporekart.modules.training.domain.exception.DemandNotFoundException ex, HttpServletRequest request) {
        log.warn("Training demand not found on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("DEMAND_NOT_FOUND", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.training.domain.exception.UnauthorizedDemandAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorizedDemandAccess(com.sporekart.modules.training.domain.exception.UnauthorizedDemandAccessException ex, HttpServletRequest request) {
        log.warn("Unauthorized demand access on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("UNAUTHORIZED_DEMAND_ACCESS", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.training.domain.exception.InvalidDemandStateException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidDemandState(com.sporekart.modules.training.domain.exception.InvalidDemandStateException ex, HttpServletRequest request) {
        log.warn("Invalid demand state on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("INVALID_DEMAND_STATE", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.training.domain.exception.PaymentVerificationException.class)
    public ResponseEntity<ApiErrorResponse> handlePaymentVerification(com.sporekart.modules.training.domain.exception.PaymentVerificationException ex, HttpServletRequest request) {
        log.warn("Training payment verification failed on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("PAYMENT_VERIFICATION_FAILED", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.training.domain.exception.InvalidPaymentStateException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidPaymentState(com.sporekart.modules.training.domain.exception.InvalidPaymentStateException ex, HttpServletRequest request) {
        log.warn("Invalid training payment state on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("INVALID_PAYMENT_STATE", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.training.domain.exception.TrainingPaymentException.class)
    public ResponseEntity<ApiErrorResponse> handleTrainingPayment(com.sporekart.modules.training.domain.exception.TrainingPaymentException ex, HttpServletRequest request) {
        log.warn("Training payment error on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("TRAINING_PAYMENT_ERROR", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.training.domain.exception.InvalidTrainingStateException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidTrainingState(com.sporekart.modules.training.domain.exception.InvalidTrainingStateException ex, HttpServletRequest request) {
        log.warn("Invalid training state on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("INVALID_TRAINING_STATE", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.training.domain.exception.InvalidBatchStateException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidBatchState(com.sporekart.modules.training.domain.exception.InvalidBatchStateException ex, HttpServletRequest request) {
        log.warn("Invalid batch state on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("INVALID_BATCH_STATE", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.training.domain.exception.CapacityExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleCapacityExceeded(com.sporekart.modules.training.domain.exception.CapacityExceededException ex, HttpServletRequest request) {
        log.warn("Training capacity exceeded on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("CAPACITY_EXCEEDED", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.training.domain.exception.InsufficientCapacityException.class)
    public ResponseEntity<ApiErrorResponse> handleInsufficientCapacity(com.sporekart.modules.training.domain.exception.InsufficientCapacityException ex, HttpServletRequest request) {
        log.warn("Insufficient capacity on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("INSUFFICIENT_CAPACITY", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.training.domain.exception.CapacityBelowOccupancyException.class)
    public ResponseEntity<ApiErrorResponse> handleCapacityBelowOccupancy(com.sporekart.modules.training.domain.exception.CapacityBelowOccupancyException ex, HttpServletRequest request) {
        log.warn("Capacity below occupancy on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("CAPACITY_BELOW_OCCUPANCY", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.training.domain.exception.UnauthorizedTrainingOperationException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorizedTrainingOperation(com.sporekart.modules.training.domain.exception.UnauthorizedTrainingOperationException ex, HttpServletRequest request) {
        log.warn("Unauthorized training operation on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("UNAUTHORIZED_TRAINING_OPERATION", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
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

    @ExceptionHandler(com.sporekart.modules.inventory.domain.exception.InsufficientStockException.class)
    public ResponseEntity<ApiErrorResponse> handleInsufficientStock(com.sporekart.modules.inventory.domain.exception.InsufficientStockException ex, HttpServletRequest request) {
        log.warn("Insufficient stock on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("INSUFFICIENT_STOCK", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.inventory.domain.exception.InventoryItemNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleInventoryItemNotFound(com.sporekart.modules.inventory.domain.exception.InventoryItemNotFoundException ex, HttpServletRequest request) {
        log.warn("Inventory item not found on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("INVENTORY_ITEM_NOT_FOUND", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.inventory.domain.exception.ReservationNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleReservationNotFound(com.sporekart.modules.inventory.domain.exception.ReservationNotFoundException ex, HttpServletRequest request) {
        log.warn("Reservation not found on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("RESERVATION_NOT_FOUND", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.inventory.domain.exception.ReservationAlreadyReleasedException.class)
    public ResponseEntity<ApiErrorResponse> handleReservationAlreadyReleased(com.sporekart.modules.inventory.domain.exception.ReservationAlreadyReleasedException ex, HttpServletRequest request) {
        log.warn("Reservation already released on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("RESERVATION_ALREADY_RELEASED", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.inventory.domain.exception.ReservationExpiredException.class)
    public ResponseEntity<ApiErrorResponse> handleReservationExpired(com.sporekart.modules.inventory.domain.exception.ReservationExpiredException ex, HttpServletRequest request) {
        log.warn("Reservation expired on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("RESERVATION_EXPIRED", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.order.domain.exception.InvalidOrderStateTransitionException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidOrderStateTransition(com.sporekart.modules.order.domain.exception.InvalidOrderStateTransitionException ex, HttpServletRequest request) {
        log.warn("Invalid order state transition on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("INVALID_ORDER_STATE_TRANSITION", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.payment.domain.exception.PaymentNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handlePaymentNotFound(com.sporekart.modules.payment.domain.exception.PaymentNotFoundException ex, HttpServletRequest request) {
        log.warn("Payment not found on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("PAYMENT_NOT_FOUND", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.payment.domain.exception.PaymentVerificationFailedException.class)
    public ResponseEntity<ApiErrorResponse> handlePaymentVerificationFailed(com.sporekart.modules.payment.domain.exception.PaymentVerificationFailedException ex, HttpServletRequest request) {
        log.warn("Payment verification failed on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("PAYMENT_VERIFICATION_FAILED", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.payment.domain.exception.PaymentInvalidStateException.class)
    public ResponseEntity<ApiErrorResponse> handlePaymentInvalidState(com.sporekart.modules.payment.domain.exception.PaymentInvalidStateException ex, HttpServletRequest request) {
        log.warn("Payment invalid state on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("PAYMENT_INVALID_STATE", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.payment.domain.exception.PaymentProviderUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handlePaymentProviderUnavailable(com.sporekart.modules.payment.domain.exception.PaymentProviderUnavailableException ex, HttpServletRequest request) {
        log.error("Payment provider unavailable on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("PAYMENT_PROVIDER_UNAVAILABLE", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.payment.domain.exception.OrderNotPayableException.class)
    public ResponseEntity<ApiErrorResponse> handleOrderNotPayable(com.sporekart.modules.payment.domain.exception.OrderNotPayableException ex, HttpServletRequest request) {
        log.warn("Order not payable on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("ORDER_NOT_PAYABLE", ex.getMessage(), request.getRequestURI());
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

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        log.warn("Unsupported Media Type on {}: {}", request.getRequestURI(), ex.getContentType());
        ApiErrorResponse response = ApiErrorResponse.of("UNSUPPORTED_MEDIA_TYPE", "Unsupported Content-Type. Only application/json is allowed", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimitExceeded(RateLimitExceededException ex, HttpServletRequest request) {
        log.warn("Rate limit exceeded on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("RATE_LIMIT_EXCEEDED", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
                .body(response);
    }

    @ExceptionHandler(PayloadTooLargeException.class)
    public ResponseEntity<ApiErrorResponse> handlePayloadTooLarge(PayloadTooLargeException ex, HttpServletRequest request) {
        log.warn("Payload too large on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("PAYLOAD_TOO_LARGE", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }

    @ExceptionHandler(UnsupportedMediaTypeException.class)
    public ResponseEntity<ApiErrorResponse> handleUnsupportedMediaType(UnsupportedMediaTypeException ex, HttpServletRequest request) {
        log.warn("Unsupported media type on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("UNSUPPORTED_MEDIA_TYPE", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response);
    }

    @ExceptionHandler(InvalidIdempotencyKeyException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidIdempotencyKey(InvalidIdempotencyKeyException ex, HttpServletRequest request) {
        log.warn("Invalid idempotency key on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("INVALID_IDEMPOTENCY_KEY", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Invalid argument on {}: {}", request.getRequestURI(), ex.getMessage());
        String code = "BAD_REQUEST";
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("sort field") || ex.getMessage().contains("sort direction") || ex.getMessage().contains("Sort field")) {
                code = "CATALOG_INVALID_SORT";
            } else if (ex.getMessage().contains("Page size") || ex.getMessage().contains("Page index") || ex.getMessage().contains("page size")) {
                code = "CATALOG_INVALID_PAGE_SIZE";
            } else if (ex.getMessage().contains("price") || ex.getMessage().contains("Price")) {
                code = "CATALOG_INVALID_PRICE_RANGE";
            } else if (ex.getMessage().contains("wildcard") || ex.getMessage().contains("Search query")) {
                code = "INVALID_SEARCH_QUERY";
            }
        }
        ApiErrorResponse response = ApiErrorResponse.of(code, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.shipment.domain.InvalidShipmentStateTransitionException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidShipmentStateTransition(com.sporekart.modules.shipment.domain.InvalidShipmentStateTransitionException ex, HttpServletRequest request) {
        log.warn("Invalid shipment state transition on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("INVALID_SHIPMENT_STATE_TRANSITION", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.returns.domain.ReturnNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleReturnNotFound(com.sporekart.modules.returns.domain.ReturnNotFoundException ex, HttpServletRequest request) {
        log.warn("Return not found on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("RETURN_NOT_FOUND", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.returns.domain.ReturnEligibilityException.class)
    public ResponseEntity<ApiErrorResponse> handleReturnEligibility(com.sporekart.modules.returns.domain.ReturnEligibilityException ex, HttpServletRequest request) {
        log.warn("Return eligibility failed on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of(ex.getReasonCode(), ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.returns.domain.InvalidReturnStateTransitionException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidReturnStateTransition(com.sporekart.modules.returns.domain.InvalidReturnStateTransitionException ex, HttpServletRequest request) {
        log.warn("Invalid return state transition on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("INVALID_RETURN_STATE_TRANSITION", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.returns.domain.ReturnAccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleReturnAccessDenied(com.sporekart.modules.returns.domain.ReturnAccessDeniedException ex, HttpServletRequest request) {
        log.warn("Return access denied on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("RETURN_ACCESS_DENIED", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.security.domain.exception.AuthenticationFailedException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationFailed(com.sporekart.modules.security.domain.exception.AuthenticationFailedException ex, HttpServletRequest request) {
        log.warn("Authentication failed on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("AUTHENTICATION_FAILED", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.security.domain.exception.AccountLockedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountLocked(com.sporekart.modules.security.domain.exception.AccountLockedException ex, HttpServletRequest request) {
        log.warn("Account locked on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("ACCOUNT_LOCKED", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.security.domain.exception.InvalidTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidToken(com.sporekart.modules.security.domain.exception.InvalidTokenException ex, HttpServletRequest request) {
        log.warn("Invalid token on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("INVALID_TOKEN", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.security.domain.exception.UserAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleUserAlreadyExists(com.sporekart.modules.security.domain.exception.UserAlreadyExistsException ex, HttpServletRequest request) {
        log.warn("User registration conflict on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("USER_ALREADY_EXISTS", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.security.domain.exception.SecurityAccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleSecurityAccessDenied(com.sporekart.modules.security.domain.exception.SecurityAccessDeniedException ex, HttpServletRequest request) {
        log.warn("Security access denied on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("ACCESS_DENIED", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(com.sporekart.modules.training.domain.exception.UnauthorizedEnrollmentAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorizedEnrollmentAccess(com.sporekart.modules.training.domain.exception.UnauthorizedEnrollmentAccessException ex, HttpServletRequest request) {
        log.warn("Unauthorized enrollment access on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("UNAUTHORIZED_ENROLLMENT_ACCESS", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {}: ", request.getRequestURI(), ex);
        ApiErrorResponse response = ApiErrorResponse.of("INTERNAL_SERVER_ERROR", "An unexpected error occurred. Please try again later.", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
