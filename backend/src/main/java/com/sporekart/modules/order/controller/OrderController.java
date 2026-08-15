package com.sporekart.modules.order.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.order.application.OrderApplicationService;
import com.sporekart.modules.order.application.dto.CreateOrderCommand;
import com.sporekart.modules.order.application.dto.OrderDto;
import com.sporekart.modules.order.application.dto.OrderSummaryDto;
import com.sporekart.modules.order.application.dto.OrderTimelineDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Customer order placement, history retrieval, timeline tracking, and cancellation")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderApplicationService orderApplicationService;

    public OrderController(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    @PostMapping
    @Operation(
            summary = "Place New Order",
            description = "Creates a new customer order from the active cart. Triggers inventory reservation and payment initiation flow. Idempotency key prevents duplicate order creation."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Order created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or cart is empty"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate order (idempotency key already used)")
    })
    public ResponseEntity<ApiResponse<OrderDto>> createOrder(
            @Valid @RequestBody CreateOrderCommand command,
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        OrderDto order = orderApplicationService.createOrder(customerId, command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(order));
    }

    @GetMapping
    @Operation(
            summary = "Get Order History",
            description = "Returns paginated order history for the authenticated customer, ordered by creation date descending. Page size is capped at 50."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order history retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<ApiResponse<Page<OrderSummaryDto>>> getOrderHistory(
            @Parameter(description = "Zero-indexed page number", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 50)", example = "10") @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        int boundedSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(Math.max(page, 0), boundedSize);

        Page<OrderSummaryDto> history = orderApplicationService.getOrderHistory(customerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/{orderReference}")
    @Operation(
            summary = "Get Order Detail",
            description = "Retrieves full detail for a single customer order. Accepts either the order UUID or the order reference string (e.g. ORD-2024-000001)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order detail retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found or does not belong to the authenticated customer")
    })
    public ResponseEntity<ApiResponse<OrderDto>> getOrderDetail(
            @Parameter(description = "Order UUID or human-readable order reference", example = "ORD-2024-000001") @PathVariable String orderReference,
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        OrderDto order;
        if (isUuid(orderReference)) {
            order = orderApplicationService.getOrderDetail(customerId, UUID.fromString(orderReference));
        } else {
            order = orderApplicationService.getOrderDetail(customerId, orderReference);
        }
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping("/{orderReference}/timeline")
    @Operation(
            summary = "Get Order Event Timeline",
            description = "Returns the chronological status event timeline for an order, showing all state transitions with timestamps and actor information."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order timeline retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<ApiResponse<OrderTimelineDto>> getOrderTimeline(
            @Parameter(description = "Order UUID or order reference", example = "ORD-2024-000001") @PathVariable String orderReference,
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        OrderTimelineDto timeline = orderApplicationService.getOrderTimeline(customerId, orderReference);
        return ResponseEntity.ok(ApiResponse.success(timeline));
    }

    @PostMapping("/{orderReference}/cancel")
    @Operation(
            summary = "Cancel Customer Order",
            description = "Cancels a customer order that is still in a cancellable state (e.g., PENDING_PAYMENT or PAYMENT_CONFIRMED). Triggers inventory release."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Order cannot be cancelled in its current state"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<ApiResponse<OrderDto>> cancelOrder(
            @Parameter(description = "Order UUID or order reference", example = "ORD-2024-000001") @PathVariable String orderReference,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        String reason = body != null ? body.get("reason") : null;
        UUID orderId;
        if (isUuid(orderReference)) {
            orderId = UUID.fromString(orderReference);
        } else {
            OrderDto existing = orderApplicationService.getOrderDetail(customerId, orderReference);
            orderId = existing.id();
        }
        OrderDto cancelledOrder = orderApplicationService.cancelOrder(customerId, orderId, reason);
        return ResponseEntity.ok(ApiResponse.success(cancelledOrder));
    }

    private boolean isUuid(String input) {
        try {
            UUID.fromString(input);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String resolveCustomerId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("User authentication required");
        }
        return authentication.getName();
    }
}
