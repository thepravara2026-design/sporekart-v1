package com.sporekart.modules.seller.web;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.security.infrastructure.jwt.UserPrincipal;
import com.sporekart.modules.seller.application.SellerApplicationService;
import com.sporekart.modules.seller.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seller")
@PreAuthorize("hasAnyRole('SELLER', 'ROLE_SELLER', 'ADMIN', 'ROLE_ADMIN')")
public class SellerController {

    private final SellerApplicationService sellerService;

    public SellerController(SellerApplicationService sellerService) {
        this.sellerService = sellerService;
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

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<SellerMetricsDto>> getDashboard(@AuthenticationPrincipal Object principal) {
        SellerMetricsDto metrics = sellerService.getMetrics(resolveUserId(principal));
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<SellerProductDto>>> getProducts(@AuthenticationPrincipal Object principal) {
        List<SellerProductDto> products = sellerService.getProducts(resolveUserId(principal));
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ApiResponse<SellerProductDto>> getProductById(
            @AuthenticationPrincipal Object principal,
            @PathVariable("id") String id
    ) {
        SellerProductDto product = sellerService.getProductById(resolveUserId(principal), id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @PostMapping("/products")
    public ResponseEntity<ApiResponse<SellerProductDto>> createProduct(
            @AuthenticationPrincipal Object principal,
            @Valid @RequestBody CreateSellerProductRequestDto dto
    ) {
        SellerProductDto created = sellerService.createProduct(resolveUserId(principal), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<List<SellerInventoryDto>>> getInventory(@AuthenticationPrincipal Object principal) {
        List<SellerInventoryDto> inventory = sellerService.getInventory(resolveUserId(principal));
        return ResponseEntity.ok(ApiResponse.success(inventory));
    }

    @PostMapping("/inventory/{sku}/adjustments")
    public ResponseEntity<ApiResponse<SellerInventoryDto>> adjustStock(
            @AuthenticationPrincipal Object principal,
            @PathVariable("sku") String sku,
            @Valid @RequestBody AdjustSellerStockRequestDto dto
    ) {
        SellerInventoryDto updated = sellerService.adjustStock(resolveUserId(principal), sku, dto);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<SellerOrderDto>>> getOrders(@AuthenticationPrincipal Object principal) {
        List<SellerOrderDto> orders = sellerService.getOrders(resolveUserId(principal));
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PostMapping("/orders/{id}/status")
    public ResponseEntity<ApiResponse<SellerOrderDto>> transitionOrder(
            @AuthenticationPrincipal Object principal,
            @PathVariable("id") String id,
            @Valid @RequestBody TransitionSellerOrderRequestDto dto
    ) {
        SellerOrderDto updated = sellerService.transitionOrder(resolveUserId(principal), id, dto.status());
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @GetMapping("/payouts")
    public ResponseEntity<ApiResponse<List<SellerPayoutDto>>> getPayouts(@AuthenticationPrincipal Object principal) {
        List<SellerPayoutDto> payouts = sellerService.getPayouts(resolveUserId(principal));
        return ResponseEntity.ok(ApiResponse.success(payouts));
    }
}
