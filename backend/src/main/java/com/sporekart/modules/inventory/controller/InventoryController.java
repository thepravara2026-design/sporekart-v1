package com.sporekart.modules.inventory.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.inventory.application.dto.InventoryItemDto;
import com.sporekart.modules.inventory.application.dto.ReservationDto;
import com.sporekart.modules.inventory.application.dto.StockAdjustmentCommand;
import jakarta.validation.Valid;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryApplicationService inventoryApplicationService;

    public InventoryController(InventoryApplicationService inventoryApplicationService) {
        this.inventoryApplicationService = inventoryApplicationService;
    }

    @GetMapping("/{sku}")
    public ResponseEntity<ApiResponse<InventoryItemDto>> getInventoryBySku(@PathVariable String sku) {
        InventoryItemDto item = inventoryApplicationService.getInventoryBySku(sku);
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    @PostMapping("/reserve/{orderId}")
    public ResponseEntity<ApiResponse<ReservationDto>> reserveInventoryForOrder(
            @PathVariable UUID orderId,
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        ReservationDto reservation = inventoryApplicationService.reserveInventoryForOrder(orderId, customerId);
        return ResponseEntity.ok(ApiResponse.success(reservation));
    }

    @PostMapping("/reservations/{reservationId}/release")
    public ResponseEntity<ApiResponse<ReservationDto>> releaseReservation(
            @PathVariable UUID reservationId,
            @RequestParam(defaultValue = "CUSTOMER_CANCELLED") String reason
    ) {
        ReservationDto released = inventoryApplicationService.releaseReservation(reservationId, reason);
        return ResponseEntity.ok(ApiResponse.success(released));
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InventoryItemDto>> adjustStock(
            @Valid @RequestBody StockAdjustmentCommand command
    ) {
        InventoryItemDto adjusted = inventoryApplicationService.adjustStock(command);
        return ResponseEntity.ok(ApiResponse.success(adjusted));
    }

    private String resolveCustomerId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("User authentication required");
        }
        return authentication.getName();
    }
}
