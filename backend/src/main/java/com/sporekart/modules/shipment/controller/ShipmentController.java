package com.sporekart.modules.shipment.controller;

import com.sporekart.modules.shipment.application.ShipmentApplicationService;
import com.sporekart.modules.shipment.application.dto.ShipmentDto;
import com.sporekart.modules.shipment.application.dto.ShipmentTrackingResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders/{orderReference}")
@Tag(name = "Shipments", description = "Customer shipment tracking and delivery status for orders")
@SecurityRequirement(name = "bearerAuth")
public class ShipmentController {

    private final ShipmentApplicationService shipmentService;

    public ShipmentController(ShipmentApplicationService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping("/shipment")
    @Operation(summary = "Get Order Shipment Details", description = "Retrieves shipment details for an order, including current status, AWB number, courier, and tracking URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shipment details retrieved"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Shipment not found for order")
    })
    public ResponseEntity<ShipmentDto> getShipmentDetails(
            @Parameter(description = "Order reference or UUID") @PathVariable String orderReference,
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        ShipmentDto shipment = shipmentService.getShipmentForCustomer(orderReference, customerId);
        return ResponseEntity.ok(shipment);
    }

    @GetMapping("/tracking")
    @Operation(summary = "Get Shipment Tracking Timeline", description = "Returns the full tracking event timeline for a shipment, including courier scan events and delivery checkpoints.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tracking timeline retrieved"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Shipment not found for order")
    })
    public ResponseEntity<ShipmentTrackingResponseDto> getTrackingTimeline(
            @Parameter(description = "Order reference or UUID") @PathVariable String orderReference,
            Authentication authentication
    ) {
        String customerId = resolveCustomerId(authentication);
        ShipmentTrackingResponseDto tracking = shipmentService.getTrackingForCustomer(orderReference, customerId);
        return ResponseEntity.ok(tracking);
    }

    private String resolveCustomerId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException(
                    "Authenticated user identity required");
        }
        return authentication.getName();
    }
}