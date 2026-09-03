package com.sporekart.modules.cart.domain;

import com.sporekart.modules.cart.domain.exception.CartItemNotFoundException;
import com.sporekart.modules.cart.domain.exception.CartNotModifiableException;
import com.sporekart.modules.cart.domain.exception.InvalidQuantityException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Cart {

    private final UUID id;
    private final String customerId;
    private CartStatus status;
    private final String currency;
    private BigDecimal subtotal;
    private int itemCount;
    private Long version;
    private final List<CartItem> items;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Cart(
            UUID id,
            String customerId,
            CartStatus status,
            String currency,
            BigDecimal subtotal,
            int itemCount,
            Long version,
            List<CartItem> items,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "Cart ID cannot be null");
        this.customerId = Objects.requireNonNull(customerId, "Customer ID cannot be null");
        this.status = Objects.requireNonNull(status, "Cart status cannot be null");
        this.currency = currency != null ? currency : "INR";
        this.version = version != null ? version : 0L;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : OffsetDateTime.now();
        recalculateTotalsInternal();
    }

    public static Cart createNewActiveCart(String customerId, String currency) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID cannot be null or blank");
        }
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        return new Cart(id, customerId, CartStatus.ACTIVE, currency != null ? currency : "INR", BigDecimal.ZERO, 0, null, new ArrayList<>(), now, now);
    }

    public CartItem addItem(
            UUID productId,
            UUID variantId,
            String sku,
            String productName,
            String variantName,
            BigDecimal unitPrice,
            int quantity,
            int maxQuantityPerItem,
            int maxTotalItems
    ) {
        ensureModifiable();
        
        Optional<CartItem> existingItemOpt = this.items.stream()
                .filter(item -> item.matchesProductAndVariant(productId, variantId))
                .findFirst();

        CartItem targetItem;
        if (existingItemOpt.isPresent()) {
            targetItem = existingItemOpt.get();
            targetItem.incrementQuantity(quantity, maxQuantityPerItem);
        } else {
            targetItem = CartItem.create(this.id, productId, variantId, sku, productName, variantName, unitPrice, quantity, maxQuantityPerItem);
            this.items.add(targetItem);
        }

        recalculateTotalsInternal();

        if (this.itemCount > maxTotalItems) {
            throw new InvalidQuantityException("Total cart items count (" + this.itemCount + ") exceeds maximum total allowed items limit of " + maxTotalItems);
        }

        this.updatedAt = OffsetDateTime.now();
        return targetItem;
    }

    public void updateItemQuantity(UUID itemId, int newQuantity, int maxQuantityPerItem, int maxTotalItems) {
        ensureModifiable();
        CartItem item = findItemById(itemId)
                .orElseThrow(() -> new CartItemNotFoundException(itemId));
        
        item.updateQuantity(newQuantity, maxQuantityPerItem);
        recalculateTotalsInternal();

        if (this.itemCount > maxTotalItems) {
            throw new InvalidQuantityException("Total cart items count (" + this.itemCount + ") exceeds maximum total allowed items limit of " + maxTotalItems);
        }

        this.updatedAt = OffsetDateTime.now();
    }

    public void removeItem(UUID itemId) {
        ensureModifiable();
        boolean removed = this.items.removeIf(item -> item.getId().equals(itemId));
        if (!removed) {
            throw new CartItemNotFoundException(itemId);
        }
        recalculateTotalsInternal();
        this.updatedAt = OffsetDateTime.now();
    }

    public void clear() {
        ensureModifiable();
        this.items.clear();
        recalculateTotalsInternal();
        this.updatedAt = OffsetDateTime.now();
    }

    public void markAsCheckedOut() {
        ensureModifiable();
        this.status = CartStatus.CHECKED_OUT;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markAsAbandoned() {
        ensureModifiable();
        this.status = CartStatus.ABANDONED;
        this.updatedAt = OffsetDateTime.now();
    }

    public boolean isActive() {
        return this.status == CartStatus.ACTIVE;
    }

    private void ensureModifiable() {
        if (this.status != CartStatus.ACTIVE) {
            throw new CartNotModifiableException(this.status);
        }
    }

    private Optional<CartItem> findItemById(UUID itemId) {
        return this.items.stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst();
    }

    private void recalculateTotalsInternal() {
        this.subtotal = this.items.stream()
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.itemCount = this.items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    // Getters
    public UUID getId() { return id; }
    public String getCustomerId() { return customerId; }
    public CartStatus getStatus() { return status; }
    public String getCurrency() { return currency; }
    public BigDecimal getSubtotal() { return subtotal; }
    public int getItemCount() { return itemCount; }
    public Long getVersion() { return version; }
    public List<CartItem> getItems() { return Collections.unmodifiableList(items); }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cart cart = (Cart) o;
        return Objects.equals(id, cart.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
