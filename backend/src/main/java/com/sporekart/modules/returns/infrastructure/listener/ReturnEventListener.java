package com.sporekart.modules.returns.infrastructure.listener;

import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.inventory.application.dto.InventoryItemDto;
import com.sporekart.modules.inventory.application.dto.StockAdjustmentCommand;
import com.sporekart.modules.returns.domain.event.ReturnAcceptedEvent;
import com.sporekart.modules.returns.domain.event.ReturnApprovedEvent;
import com.sporekart.modules.shipment.application.ShipmentApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ReturnEventListener {

    private static final Logger log = LoggerFactory.getLogger(ReturnEventListener.class);

    private final ShipmentApplicationService shipmentApplicationService;
    private final InventoryApplicationService inventoryApplicationService;

    public ReturnEventListener(
            ShipmentApplicationService shipmentApplicationService,
            InventoryApplicationService inventoryApplicationService
    ) {
        this.shipmentApplicationService = shipmentApplicationService;
        this.inventoryApplicationService = inventoryApplicationService;
    }

    @EventListener
    @Async
    public void onReturnApproved(ReturnApprovedEvent event) {
        log.info("Handling ReturnApprovedEvent for return {} (Order {})", event.returnReference(), event.orderReference());
        try {
            // Create reverse shipment via Sprint 3G shipping service
            shipmentApplicationService.createShipmentForOrder(event.orderId());
            log.info("Successfully created reverse shipment for return {}", event.returnReference());
        } catch (Exception e) {
            log.warn("Auto reverse shipment creation deferred or already exists for order {}: {}", event.orderReference(), e.getMessage());
        }
    }

    @EventListener
    @Async
    public void onReturnAccepted(ReturnAcceptedEvent event) {
        log.info("Handling ReturnAcceptedEvent for return {} with total refund amount {}", event.returnReference(), event.totalRefundAmount());
        for (ReturnAcceptedEvent.AcceptedItemPayload item : event.acceptedItems()) {
            try {
                InventoryItemDto currentInv = inventoryApplicationService.getInventoryBySku(item.sku());
                int newOnHand = currentInv.onHandQuantity() + item.acceptedQuantity();
                inventoryApplicationService.adjustStock(new StockAdjustmentCommand(item.sku(), newOnHand, "RETURN_ACCEPTED_RESTOCK"));
                log.info("Restored {} units of SKU {} to inventory following return acceptance", item.acceptedQuantity(), item.sku());
            } catch (Exception e) {
                log.error("Failed to restore inventory for SKU {}: {}", item.sku(), e.getMessage());
            }
        }
    }
}
