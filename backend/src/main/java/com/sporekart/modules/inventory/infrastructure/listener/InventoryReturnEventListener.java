package com.sporekart.modules.inventory.infrastructure.listener;

import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.inventory.application.dto.InventoryItemDto;
import com.sporekart.modules.inventory.application.dto.StockAdjustmentCommand;
import com.sporekart.modules.returns.domain.event.ReturnAcceptedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryReturnEventListener {

    private static final Logger log = LoggerFactory.getLogger(InventoryReturnEventListener.class);

    private final InventoryApplicationService inventoryApplicationService;

    public InventoryReturnEventListener(InventoryApplicationService inventoryApplicationService) {
        this.inventoryApplicationService = inventoryApplicationService;
    }

    @EventListener
    public void onReturnAcceptedEvent(ReturnAcceptedEvent event) {
        log.info("Inventory listener received ReturnAcceptedEvent for return {} (orderRef: {})", event.returnReference(), event.orderReference());

        if (event.acceptedItems() == null || event.acceptedItems().isEmpty()) {
            log.info("No items to restock for accepted return {}", event.returnReference());
            return;
        }

        for (ReturnAcceptedEvent.AcceptedItemPayload item : event.acceptedItems()) {
            if (item.acceptedQuantity() <= 0) {
                continue;
            }
            try {
                InventoryItemDto existing = inventoryApplicationService.getInventoryBySku(item.sku());
                int newOnHand = existing.onHandQuantity() + item.acceptedQuantity();
                inventoryApplicationService.adjustStock(new StockAdjustmentCommand(
                        item.sku(),
                        newOnHand,
                        "RESTOCK_RETURN_" + event.returnReference()
                ));
                log.info("Successfully restocked SKU {} by quantity {} for return {}", item.sku(), item.acceptedQuantity(), event.returnReference());
            } catch (Exception e) {
                log.error("Failed to restock inventory for SKU {} on return {}: {}", item.sku(), event.returnReference(), e.getMessage());
            }
        }
    }
}
