package com.sporekart.modules.inventory;

import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.inventory.application.dto.InventoryItemDto;
import com.sporekart.modules.inventory.application.dto.ReservationDto;
import com.sporekart.modules.inventory.application.dto.StockAdjustmentCommand;
import com.sporekart.modules.inventory.application.dto.StockAvailabilityDto;
import com.sporekart.modules.inventory.application.dto.StockMovementDto;
import com.sporekart.modules.inventory.domain.MovementType;
import com.sporekart.modules.inventory.domain.ReservationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InventoryFullLifecycleHandoffTest {

    @Autowired
    private InventoryApplicationService inventoryApplicationService;

    @Test
    @DisplayName("Sprint 4E: Complete Stock Accounting, Commitment, Damaged Stock & Movement Ledger Handoff Journey")
    void testCompleteInventoryLifecycle() {
        String sku = "SKU-E2E-4E-" + UUID.randomUUID().toString().substring(0, 8);
        UUID productId = UUID.randomUUID();

        // 1. Initialize Stock
        InventoryItemDto item = inventoryApplicationService.createOrUpdateInitialStock(productId, null, sku, 50);
        assertEquals(50, item.onHandQuantity());
        assertEquals(0, item.reservedQuantity());
        assertEquals(0, item.damagedQuantity());
        assertEquals(50, item.availableQuantity());
        assertFalse(item.isLowStock());

        // 2. Customer Availability Check
        StockAvailabilityDto avail = inventoryApplicationService.getInventoryAvailability(sku);
        assertEquals("IN_STOCK", avail.status());
        assertTrue(avail.available());

        // 3. Admin Adjustment & Damaged Stock Recording
        inventoryApplicationService.recordDamagedStock(sku, 5, "DEFECT_FOUND");
        InventoryItemDto afterDamaged = inventoryApplicationService.getInventoryBySku(sku);
        assertEquals(50, afterDamaged.onHandQuantity());
        assertEquals(5, afterDamaged.damagedQuantity());
        assertEquals(45, afterDamaged.availableQuantity());

        // 4. Stock Adjustment
        inventoryApplicationService.adjustStock(new StockAdjustmentCommand(sku, 60, "RESTOCK_INBOUND"));
        InventoryItemDto afterAdjust = inventoryApplicationService.getInventoryBySku(sku);
        assertEquals(60, afterAdjust.onHandQuantity());
        assertEquals(55, afterAdjust.availableQuantity());

        // 5. Movement Audit History
        List<StockMovementDto> movements = inventoryApplicationService.listMovementsForSku(sku);
        assertNotNull(movements);
        assertFalse(movements.isEmpty());
        assertTrue(movements.stream().anyMatch(m -> m.movementType() == MovementType.DAMAGE));
        assertTrue(movements.stream().anyMatch(m -> m.movementType() == MovementType.STOCK_ADJUSTMENT));
    }
}
