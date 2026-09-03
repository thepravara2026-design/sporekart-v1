package com.sporekart.modules.inventory.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.inventory.application.dto.InventoryItemDto;
import com.sporekart.modules.inventory.application.dto.StockAdjustmentCommand;
import com.sporekart.modules.inventory.application.dto.StockMovementDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/inventory")
@PreAuthorize("hasAnyRole('ADMIN', 'ROLE_ADMIN')")
public class AdminInventoryController {

    private final InventoryApplicationService inventoryApplicationService;

    public AdminInventoryController(InventoryApplicationService inventoryApplicationService) {
        this.inventoryApplicationService = inventoryApplicationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryItemDto>>> listInventory() {
        List<InventoryItemDto> items = inventoryApplicationService.listAllInventory();
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @GetMapping("/{sku}")
    public ResponseEntity<ApiResponse<InventoryItemDto>> getInventoryBySku(@PathVariable String sku) {
        InventoryItemDto item = inventoryApplicationService.getInventoryBySku(sku);
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    @GetMapping("/{sku}/movements")
    public ResponseEntity<ApiResponse<List<StockMovementDto>>> listMovementsForSku(@PathVariable String sku) {
        List<StockMovementDto> movements = inventoryApplicationService.listMovementsForSku(sku);
        return ResponseEntity.ok(ApiResponse.success(movements));
    }

    @PostMapping("/{sku}/adjustments")
    public ResponseEntity<ApiResponse<InventoryItemDto>> adjustStock(
            @PathVariable String sku,
            @Valid @RequestBody StockAdjustmentCommand command
    ) {
        // Ensure SKU matches path
        StockAdjustmentCommand cmd = new StockAdjustmentCommand(
                sku, command.newOnHandQuantity(), command.reason()
        );
        InventoryItemDto adjusted = inventoryApplicationService.adjustStock(cmd);
        return ResponseEntity.ok(ApiResponse.success(adjusted));
    }

    @PostMapping("/{sku}/damaged")
    public ResponseEntity<ApiResponse<InventoryItemDto>> recordDamagedStock(
            @PathVariable String sku,
            @RequestParam int quantity,
            @RequestParam(required = false, defaultValue = "WAREHOUSE_QA_DEFECT") String reason
    ) {
        InventoryItemDto item = inventoryApplicationService.recordDamagedStock(sku, quantity, reason);
        return ResponseEntity.ok(ApiResponse.success(item));
    }
}
