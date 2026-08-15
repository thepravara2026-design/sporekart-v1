package com.sporekart.modules.order.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.order.application.OrderApplicationService;
import com.sporekart.modules.order.application.dto.OrderDto;
import com.sporekart.modules.order.application.dto.OrderSummaryDto;
import com.sporekart.modules.order.application.dto.OrderTimelineDto;
import com.sporekart.modules.order.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderApplicationService orderApplicationService;

    public AdminOrderController(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderSummaryDto>>> listAdminOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) OrderStatus status
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<OrderSummaryDto> orders = orderApplicationService.getAdminOrderList(pageable, status);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderDto>> getAdminOrderDetail(@PathVariable String orderNumber) {
        OrderDto order = orderApplicationService.getAdminOrderDetail(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping("/{orderNumber}/timeline")
    public ResponseEntity<ApiResponse<OrderTimelineDto>> getAdminOrderTimeline(@PathVariable String orderNumber) {
        OrderTimelineDto timeline = orderApplicationService.getAdminOrderTimeline(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(timeline));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderDto>> cancelOrderAsAdmin(
            @PathVariable UUID orderId,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication
    ) {
        String adminId = resolveAdminId(authentication);
        String reason = body != null ? body.get("reason") : "Administrative cancellation";
        OrderDto cancelled = orderApplicationService.cancelOrderAsAdmin(adminId, orderId, reason);
        return ResponseEntity.ok(ApiResponse.success(cancelled));
    }

    @PostMapping("/{orderId}/process")
    public ResponseEntity<ApiResponse<OrderDto>> startProcessing(@PathVariable UUID orderId, Authentication authentication) {
        String adminId = resolveAdminId(authentication);
        OrderDto updated = orderApplicationService.startProcessing(orderId, adminId);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @PostMapping("/{orderId}/ready-for-fulfilment")
    public ResponseEntity<ApiResponse<OrderDto>> markReadyForFulfilment(@PathVariable UUID orderId, Authentication authentication) {
        String adminId = resolveAdminId(authentication);
        OrderDto updated = orderApplicationService.markReadyForFulfilment(orderId, adminId);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @PostMapping("/{orderId}/shipped")
    public ResponseEntity<ApiResponse<OrderDto>> markShipped(
            @PathVariable UUID orderId,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication
    ) {
        String adminId = resolveAdminId(authentication);
        String trackingNumber = body != null ? body.get("trackingNumber") : null;
        OrderDto updated = orderApplicationService.markShipped(orderId, adminId, trackingNumber);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @PostMapping("/{orderId}/out-for-delivery")
    public ResponseEntity<ApiResponse<OrderDto>> markOutForDelivery(@PathVariable UUID orderId, Authentication authentication) {
        String adminId = resolveAdminId(authentication);
        OrderDto updated = orderApplicationService.markOutForDelivery(orderId, adminId);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @PostMapping("/{orderId}/delivered")
    public ResponseEntity<ApiResponse<OrderDto>> markDelivered(@PathVariable UUID orderId, Authentication authentication) {
        String adminId = resolveAdminId(authentication);
        OrderDto updated = orderApplicationService.markDelivered(orderId, adminId);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @PostMapping("/{orderId}/complete")
    public ResponseEntity<ApiResponse<OrderDto>> markCompleted(@PathVariable UUID orderId, Authentication authentication) {
        String adminId = resolveAdminId(authentication);
        OrderDto updated = orderApplicationService.markCompleted(orderId, adminId);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    private String resolveAdminId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return "ADMIN";
        }
        return authentication.getName();
    }
}
