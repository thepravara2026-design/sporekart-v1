package com.sporekart.modules.shipment.controller;

import com.sporekart.modules.shipment.application.ShipmentApplicationService;
import com.sporekart.modules.shipment.domain.ShipmentProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/webhooks/shipping")
public class ShippingWebhookController {

    private static final Logger log = LoggerFactory.getLogger(ShippingWebhookController.class);

    private final ShipmentApplicationService shipmentService;

    public ShippingWebhookController(ShipmentApplicationService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping("/{provider}")
    public ResponseEntity<Map<String, String>> handleWebhook(
            @PathVariable String provider,
            @RequestBody String rawBody,
            @RequestHeader Map<String, String> headers
    ) {
        log.info("Received shipping webhook notification for provider: {}", provider);
        ShipmentProviderType providerType;
        try {
            providerType = ShipmentProviderType.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Unknown shipping provider webhook: {}", provider);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "ERROR", "message", "Unknown shipping provider"));
        }

        boolean success = shipmentService.processWebhook(providerType, rawBody, headers);
        if (!success) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "ERROR", "message", "Invalid webhook signature"));
        }

        return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Webhook processed successfully"));
    }
}
