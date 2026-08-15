package com.sporekart.modules.inventory.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.inventory.application.dto.StockAvailabilityDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
public class CustomerInventoryController {

    private final InventoryApplicationService inventoryApplicationService;

    public CustomerInventoryController(InventoryApplicationService inventoryApplicationService) {
        this.inventoryApplicationService = inventoryApplicationService;
    }

    @GetMapping("/skus/{sku}/availability")
    public ResponseEntity<ApiResponse<StockAvailabilityDto>> getStockAvailability(@PathVariable String sku) {
        StockAvailabilityDto availability = inventoryApplicationService.getInventoryAvailability(sku);
        return ResponseEntity.ok(ApiResponse.success(availability));
    }
}
