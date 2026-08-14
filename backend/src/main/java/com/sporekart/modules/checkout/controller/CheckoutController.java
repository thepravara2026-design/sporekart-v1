package com.sporekart.modules.checkout.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.cart.domain.exception.CartAccessDeniedException;
import com.sporekart.modules.checkout.application.CheckoutApplicationService;
import com.sporekart.modules.checkout.application.dto.CheckoutPreviewRequest;
import com.sporekart.modules.checkout.application.dto.CheckoutPreviewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/checkout")
@Tag(name = "Checkout", description = "Server-Authoritative Pricing & Checkout Calculation Operations")
public class CheckoutController {

    private final CheckoutApplicationService checkoutApplicationService;

    public CheckoutController(CheckoutApplicationService checkoutApplicationService) {
        this.checkoutApplicationService = checkoutApplicationService;
    }

    @PostMapping("/preview")
    @Operation(
            summary = "Generate Checkout Preview",
            description = "Calculates authoritative server-side pricing, line totals, tax, shipping, and grand total for the customer's active cart. Does NOT create an Order."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Checkout preview calculated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cart is empty, inactive, or invalid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Active cart not found for authenticated customer"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated request")
    })
    public ResponseEntity<ApiResponse<CheckoutPreviewResponse>> generateCheckoutPreview(
            @Valid @RequestBody(required = false) CheckoutPreviewRequest request,
            Principal principal
    ) {
        String customerId = resolveCustomerId(principal);
        CheckoutPreviewResponse preview = checkoutApplicationService.generateCheckoutPreview(customerId, request);
        return ResponseEntity.ok(ApiResponse.success(preview));
    }

    private String resolveCustomerId(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new CartAccessDeniedException("Authentication required to generate checkout preview");
        }
        return principal.getName();
    }
}
