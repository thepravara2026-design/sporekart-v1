package com.sporekart.modules.shipment.controller;

import com.sporekart.modules.order.domain.OrderActorType;
import com.sporekart.modules.shipment.application.ShipmentApplicationService;
import com.sporekart.modules.shipment.application.dto.ShipmentDto;
import com.sporekart.modules.shipment.domain.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/shipments")
public class AdminShipmentController {

    private final ShipmentApplicationService shipmentService;

    public AdminShipmentController(ShipmentApplicationService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping
    public ResponseEntity<Page<ShipmentDto>> getShipmentList(
            @RequestParam(required = false) ShipmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<ShipmentDto> shipments = shipmentService.getAdminShipments(
                status,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return ResponseEntity.ok(shipments);
    }

    @GetMapping("/{shipmentReference}")
    public ResponseEntity<ShipmentDto> getShipmentDetail(@PathVariable String shipmentReference) {
        ShipmentDto shipment = shipmentService.getAdminShipmentByReference(shipmentReference);
        return ResponseEntity.ok(shipment);
    }

    @PostMapping("/{shipmentReference}/retry")
    public ResponseEntity<ShipmentDto> retryBooking(@PathVariable String shipmentReference) {
        ShipmentDto existing = shipmentService.getAdminShipmentByReference(shipmentReference);
        ShipmentDto retried = shipmentService.bookShipment(existing.id());
        return ResponseEntity.ok(retried);
    }

    @PostMapping("/{shipmentReference}/cancel")
    public ResponseEntity<ShipmentDto> cancelShipment(
            @PathVariable String shipmentReference,
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(value = "X-Admin-Id", defaultValue = "admin-1001") String adminId
    ) {
        String reason = body != null ? body.get("reason") : "Admin cancelled shipment";
        ShipmentDto cancelled = shipmentService.cancelShipment(shipmentReference, reason, OrderActorType.ADMIN, adminId);
        return ResponseEntity.ok(cancelled);
    }

    @PostMapping("/{shipmentReference}/sync")
    public ResponseEntity<ShipmentDto> syncShipment(@PathVariable String shipmentReference) {
        ShipmentDto synced = shipmentService.syncShipmentWithProvider(shipmentReference);
        return ResponseEntity.ok(synced);
    }

    @GetMapping("/{shipmentReference}/label")
    public ResponseEntity<Map<String, String>> getLabelUrl(@PathVariable String shipmentReference) {
        String labelUrl = shipmentService.getShipmentLabelUrl(shipmentReference);
        return ResponseEntity.ok(Map.of("labelUrl", labelUrl));
    }

    @GetMapping("/{shipmentReference}/manifest")
    public ResponseEntity<Map<String, String>> getManifestUrl(@PathVariable String shipmentReference) {
        String manifestUrl = shipmentService.getShipmentManifestUrl(shipmentReference);
        return ResponseEntity.ok(Map.of("manifestUrl", manifestUrl));
    }

    @PostMapping("/reconcile")
    public ResponseEntity<Map<String, Object>> forceReconcile() {
        shipmentService.reconcileActiveShipments();
        return ResponseEntity.ok(Map.of("message", "Reconciliation triggered successfully", "status", "SUCCESS"));
    }
}
