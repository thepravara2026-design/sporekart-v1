package com.sporekart.modules.order.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.order.application.OrderApplicationService;
import com.sporekart.modules.order.application.dto.CreateOrderCommand;
import com.sporekart.modules.order.application.dto.OrderDto;
import com.sporekart.modules.order.application.dto.OrderSummaryDto;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderApplicationService orderApplicationService;

    public OrderController(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto>> createOrder(
            @Valid @RequestBody CreateOrderCommand command,
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        OrderDto order = orderApplicationService.createOrder(customerId, command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(order));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderSummaryDto>>> getOrderHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        int boundedSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(Math.max(page, 0), boundedSize);

        Page<OrderSummaryDto> history = orderApplicationService.getOrderHistory(customerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderDetail(
            @PathVariable UUID orderId,
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        OrderDto order = orderApplicationService.getOrderDetail(customerId, orderId);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderDto>> cancelOrder(
            @PathVariable UUID orderId,
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        OrderDto cancelledOrder = orderApplicationService.cancelOrder(customerId, orderId);
        return ResponseEntity.ok(ApiResponse.success(cancelledOrder));
    }

    private String resolveCustomerId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("User authentication required");
        }
        return authentication.getName();
    }
}
