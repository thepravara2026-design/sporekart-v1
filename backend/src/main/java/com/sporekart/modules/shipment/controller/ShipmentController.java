package com.sporekart.modules.shipment.controller;

import com.sporekart.modules.shipment.application.ShipmentApplicationService;
import com.sporekart.modules.shipment.application.dto.ShipmentDto;
import com.sporekart.modules.shipment.application.dto.ShipmentTrackingResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders/{orderReference}")
public class ShipmentController {

    private final ShipmentApplicationService shipmentService;

    public ShipmentController(ShipmentApplicationService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping("/shipment")
    public ResponseEntity<ShipmentDto> getShipmentDetails(
            @PathVariable String orderReference,
            @RequestHeader(value = "X-Customer-Id", defaultValue = "cust-1001") String customerId
    ) {
        ShipmentDto shipment = shipmentService.getShipmentForCustomer(orderReference, customerId);
        return ResponseEntity.ok(shipment);
    }

    @GetMapping("/tracking")
    public ResponseEntity<ShipmentTrackingResponseDto> getTrackingTimeline(
            @PathVariable String orderReference,
            @RequestHeader(value = "X-Customer-Id", defaultValue = "cust-1001") String customerId
    ) {
        ShipmentTrackingResponseDto tracking = shipmentService.getTrackingForCustomer(orderReference, customerId);
        return ResponseEntity.ok(tracking);
    }
}
