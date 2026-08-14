package com.sporekart.modules.cart.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.cart.application.CartApplicationService;
import com.sporekart.modules.cart.application.dto.AddCartItemCommand;
import com.sporekart.modules.cart.application.dto.CartDto;
import com.sporekart.modules.cart.application.dto.UpdateCartItemCommand;
import com.sporekart.modules.cart.domain.exception.CartAccessDeniedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Cart", description = "Customer Active Cart Management Operations")
public class CartController {

    private final CartApplicationService cartApplicationService;

    public CartController(CartApplicationService cartApplicationService) {
        this.cartApplicationService = cartApplicationService;
    }

    @GetMapping
    @Operation(summary = "Get Customer Active Cart", description = "Retrieves or creates the active shopping cart for the authenticated customer")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Active cart retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated request")
    })
    public ResponseEntity<ApiResponse<CartDto>> getCart(Principal principal) {
        String customerId = resolveCustomerId(principal);
        CartDto cart = cartApplicationService.getOrCreateActiveCart(customerId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @PostMapping("/items")
    @Operation(summary = "Add Item to Cart", description = "Adds a catalog product item to the authenticated customer's active cart")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Item added to cart successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or quantity"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Catalog product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated request")
    })
    public ResponseEntity<ApiResponse<CartDto>> addItem(
            @Valid @RequestBody AddCartItemCommand command,
            Principal principal
    ) {
        String customerId = resolveCustomerId(principal);
        CartDto cart = cartApplicationService.addItemToCart(customerId, command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(cart));
    }

    @PatchMapping("/items/{itemId}")
    @Operation(summary = "Update Cart Item Quantity", description = "Updates quantity for an existing item line in the active cart")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item quantity updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid quantity specified"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cart item not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated request")
    })
    public ResponseEntity<ApiResponse<CartDto>> updateItemQuantity(
            @Parameter(description = "Cart Item ID", required = true) @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemCommand command,
            Principal principal
    ) {
        String customerId = resolveCustomerId(principal);
        CartDto cart = cartApplicationService.updateCartItemQuantity(customerId, itemId, command);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove Cart Item", description = "Removes a specific line item from the authenticated customer's active cart")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item removed from cart successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cart item not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated request")
    })
    public ResponseEntity<ApiResponse<CartDto>> removeItem(
            @Parameter(description = "Cart Item ID", required = true) @PathVariable UUID itemId,
            Principal principal
    ) {
        String customerId = resolveCustomerId(principal);
        CartDto cart = cartApplicationService.removeCartItem(customerId, itemId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @DeleteMapping("/items")
    @Operation(summary = "Clear Active Cart", description = "Removes all line items from the authenticated customer's active cart")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cart cleared successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated request")
    })
    public ResponseEntity<ApiResponse<CartDto>> clearCart(Principal principal) {
        String customerId = resolveCustomerId(principal);
        CartDto cart = cartApplicationService.clearCart(customerId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    private String resolveCustomerId(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new CartAccessDeniedException("Authentication required to access shopping cart");
        }
        return principal.getName();
    }
}
