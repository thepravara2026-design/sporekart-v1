package com.sporekart.modules.grower.web;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.catalog.domain.product.Product;
import com.sporekart.modules.grower.application.GrowerApplicationService;
import com.sporekart.modules.grower.web.dto.*;
import com.sporekart.modules.inventory.domain.InventoryItem;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.security.infrastructure.jwt.UserPrincipal;
import com.sporekart.modules.shipment.domain.Shipment;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/grower")
@PreAuthorize("hasAnyRole('GROWER', 'ROLE_GROWER', 'ADMIN', 'ROLE_ADMIN')")
public class GrowerController {

    private final GrowerApplicationService growerService;

    public GrowerController(GrowerApplicationService growerService) {
        this.growerService = growerService;
    }

    private String resolveUserId(Object principal) {
        if (principal instanceof UserPrincipal up) {
            return up.getId();
        }
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            return ud.getUsername();
        }
        if (principal instanceof String str && !str.isBlank()) {
            return str;
        }
        throw new org.springframework.security.access.AccessDeniedException("User must be authenticated");
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<GrowerProfileDto>> getProfile(@AuthenticationPrincipal Object principal) {
        GrowerProfileDto profile = growerService.getProfile(resolveUserId(principal));
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<GrowerProfileDto>> updateProfile(
            @AuthenticationPrincipal Object principal,
            @RequestBody GrowerProfileDto dto
    ) {
        GrowerProfileDto updated = growerService.updateProfile(resolveUserId(principal), dto);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<GrowerSettingsDto>> getSettings(@AuthenticationPrincipal Object principal) {
        GrowerSettingsDto settings = growerService.getSettings(resolveUserId(principal));
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @PutMapping("/settings")
    public ResponseEntity<ApiResponse<GrowerSettingsDto>> updateSettings(
            @AuthenticationPrincipal Object principal,
            @RequestBody GrowerSettingsDto dto
    ) {
        GrowerSettingsDto updated = growerService.updateSettings(resolveUserId(principal), dto);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<GrowerDashboardDto>> getDashboard(@AuthenticationPrincipal Object principal) {
        GrowerDashboardDto dashboard = growerService.getDashboard(resolveUserId(principal));
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<Product>>> getProducts(@AuthenticationPrincipal Object principal) {
        List<Product> products = growerService.getProducts(resolveUserId(principal));
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ApiResponse<Product>> getProductById(
            @AuthenticationPrincipal Object principal,
            @PathVariable("id") UUID id
    ) {
        Product product = growerService.getProductById(resolveUserId(principal), id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @PostMapping("/products")
    public ResponseEntity<ApiResponse<Product>> createProduct(
            @AuthenticationPrincipal Object principal,
            @RequestBody CreateGrowerProductRequestDto dto
    ) {
        Product created = growerService.createProduct(resolveUserId(principal), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @AuthenticationPrincipal Object principal,
            @PathVariable("id") UUID id,
            @RequestBody CreateGrowerProductRequestDto dto
    ) {
        Product updated = growerService.updateProduct(resolveUserId(principal), id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @PutMapping("/products/{id}/images")
    public ResponseEntity<ApiResponse<Product>> updateProductImages(
            @AuthenticationPrincipal Object principal,
            @PathVariable("id") UUID id,
            @RequestBody UpdateProductImagesRequestDto dto
    ) {
        boolean isAdmin = principal instanceof UserPrincipal up && up.getRole() == com.sporekart.modules.security.domain.UserRole.ROLE_ADMIN;
        Product updated = growerService.updateProductImages(resolveUserId(principal), id, dto.imageUrls(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<List<InventoryItem>>> getInventory(@AuthenticationPrincipal Object principal) {
        List<InventoryItem> inventory = growerService.getInventory(resolveUserId(principal));
        return ResponseEntity.ok(ApiResponse.success(inventory));
    }

    @GetMapping("/inventory/{sku}")
    public ResponseEntity<ApiResponse<InventoryItem>> getInventoryBySku(
            @AuthenticationPrincipal Object principal,
            @PathVariable("sku") String sku
    ) {
        InventoryItem item = growerService.getInventoryBySku(resolveUserId(principal), sku);
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    @PostMapping("/inventory/{sku}/adjustments")
    public ResponseEntity<ApiResponse<InventoryItem>> adjustStock(
            @AuthenticationPrincipal Object principal,
            @PathVariable("sku") String sku,
            @RequestBody AdjustStockRequestDto dto
    ) {
        InventoryItem updated = growerService.adjustStock(resolveUserId(principal), sku, dto);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<Order>>> getOrders(@AuthenticationPrincipal Object principal) {
        List<Order> orders = growerService.getOrders(resolveUserId(principal));
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<ApiResponse<Order>> getOrderById(
            @AuthenticationPrincipal Object principal,
            @PathVariable("id") UUID id
    ) {
        Order order = growerService.getOrderById(resolveUserId(principal), id);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PostMapping("/orders/{id}/process")
    public ResponseEntity<ApiResponse<Order>> processOrder(
            @AuthenticationPrincipal Object principal,
            @PathVariable("id") UUID id
    ) {
        Order updated = growerService.transitionOrder(resolveUserId(principal), id, OrderStatus.PROCESSING);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @PostMapping("/orders/{id}/ready-for-fulfilment")
    public ResponseEntity<ApiResponse<Order>> readyForFulfillment(
            @AuthenticationPrincipal Object principal,
            @PathVariable("id") UUID id
    ) {
        Order updated = growerService.transitionOrder(resolveUserId(principal), id, OrderStatus.READY_FOR_FULFILMENT);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @PostMapping("/orders/{id}/shipped")
    public ResponseEntity<ApiResponse<Order>> markShipped(
            @AuthenticationPrincipal Object principal,
            @PathVariable("id") UUID id
    ) {
        Order updated = growerService.transitionOrder(resolveUserId(principal), id, OrderStatus.SHIPPED);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @GetMapping("/shipments")
    public ResponseEntity<ApiResponse<List<Shipment>>> getShipments(@AuthenticationPrincipal Object principal) {
        List<Shipment> shipments = growerService.getShipments(resolveUserId(principal));
        return ResponseEntity.ok(ApiResponse.success(shipments));
    }

    @GetMapping("/reports/summary")
    public ResponseEntity<ApiResponse<GrowerReportSummaryDto>> getReportSummary(
            @AuthenticationPrincipal Object principal,
            @RequestParam(name = "period", required = false, defaultValue = "THIS_MONTH") String period
    ) {
        GrowerReportSummaryDto summary = growerService.getReportSummary(resolveUserId(principal), period);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
