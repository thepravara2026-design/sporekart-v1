package com.sporekart.modules.cart.domain;

import com.sporekart.modules.cart.domain.exception.InvalidQuantityException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public class CartItem {

    private final UUID id;
    private final UUID cartId;
    private final UUID productId;
    private final UUID variantId;
    private final String sku;
    private final String productNameSnapshot;
    private final String variantNameSnapshot;
    private final BigDecimal unitPriceSnapshot;
    private int quantity;
    private BigDecimal lineTotal;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public CartItem(
            UUID id,
            UUID cartId,
            UUID productId,
            UUID variantId,
            String sku,
            String productNameSnapshot,
            String variantNameSnapshot,
            BigDecimal unitPriceSnapshot,
            int quantity,
            BigDecimal lineTotal,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "CartItem ID cannot be null");
        this.cartId = Objects.requireNonNull(cartId, "Cart ID cannot be null");
        this.productId = Objects.requireNonNull(productId, "Product ID cannot be null");
        this.variantId = variantId;
        this.sku = Objects.requireNonNull(sku, "SKU cannot be null");
        this.productNameSnapshot = Objects.requireNonNull(productNameSnapshot, "Product name snapshot cannot be null");
        this.variantNameSnapshot = variantNameSnapshot;
        this.unitPriceSnapshot = Objects.requireNonNull(unitPriceSnapshot, "Unit price snapshot cannot be null");
        
        validateQuantity(quantity);
        this.quantity = quantity;
        this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : OffsetDateTime.now();
        this.lineTotal = lineTotal != null ? lineTotal : calculateLineTotal(this.unitPriceSnapshot, this.quantity);
    }

    public static CartItem create(
            UUID cartId,
            UUID productId,
            UUID variantId,
            String sku,
            String productNameSnapshot,
            String variantNameSnapshot,
            BigDecimal unitPriceSnapshot,
            int quantity,
            int maxQuantityPerItem
    ) {
        if (quantity > maxQuantityPerItem) {
            throw new InvalidQuantityException("Item quantity " + quantity + " exceeds maximum allowed limit of " + maxQuantityPerItem);
        }
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        BigDecimal initialLineTotal = calculateLineTotal(unitPriceSnapshot, quantity);
        return new CartItem(id, cartId, productId, variantId, sku, productNameSnapshot, variantNameSnapshot, unitPriceSnapshot, quantity, initialLineTotal, now, now);
    }

    public void updateQuantity(int newQuantity, int maxQuantityPerItem) {
        validateQuantity(newQuantity);
        if (newQuantity > maxQuantityPerItem) {
            throw new InvalidQuantityException("Requested quantity " + newQuantity + " exceeds maximum allowed limit of " + maxQuantityPerItem);
        }
        this.quantity = newQuantity;
        this.lineTotal = calculateLineTotal(this.unitPriceSnapshot, this.quantity);
        this.updatedAt = OffsetDateTime.now();
    }

    public void incrementQuantity(int addQuantity, int maxQuantityPerItem) {
        validateQuantity(addQuantity);
        int targetQuantity;
        try {
            targetQuantity = Math.addExact(this.quantity, addQuantity);
        } catch (ArithmeticException e) {
            throw new InvalidQuantityException("Quantity overflow detected");
        }
        if (targetQuantity > maxQuantityPerItem) {
            throw new InvalidQuantityException("Adding quantity " + addQuantity + " would cause line item total (" + targetQuantity + ") to exceed maximum allowed limit of " + maxQuantityPerItem);
        }
        this.quantity = targetQuantity;
        this.lineTotal = calculateLineTotal(this.unitPriceSnapshot, this.quantity);
        this.updatedAt = OffsetDateTime.now();
    }

    public boolean matchesProductAndVariant(UUID targetProductId, UUID targetVariantId) {
        if (!this.productId.equals(targetProductId)) {
            return false;
        }
        return Objects.equals(this.variantId, targetVariantId);
    }

    private static void validateQuantity(int q) {
        if (q <= 0) {
            throw new InvalidQuantityException("Quantity must be greater than zero. Received: " + q);
        }
    }

    private static BigDecimal calculateLineTotal(BigDecimal price, int qty) {
        return price.multiply(BigDecimal.valueOf(qty));
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getCartId() { return cartId; }
    public UUID getProductId() { return productId; }
    public UUID getVariantId() { return variantId; }
    public String getSku() { return sku; }
    public String getProductNameSnapshot() { return productNameSnapshot; }
    public String getVariantNameSnapshot() { return variantNameSnapshot; }
    public BigDecimal getUnitPriceSnapshot() { return unitPriceSnapshot; }
    public int getQuantity() { return quantity; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return Objects.equals(id, cartItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
