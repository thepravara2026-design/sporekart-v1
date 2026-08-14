package com.sporekart.modules.order.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public final class OrderItem {

    private final UUID id;
    private final UUID orderId;
    private final UUID productId;
    private final UUID variantId;
    private final String sku;
    private final String productNameSnapshot;
    private final String variantNameSnapshot;
    private final BigDecimal unitPrice;
    private final int quantity;
    private final BigDecimal discountAmount;
    private final BigDecimal taxAmount;
    private final BigDecimal lineSubtotal;
    private final BigDecimal lineTotal;
    private final OffsetDateTime createdAt;

    public OrderItem(
            UUID id,
            UUID orderId,
            UUID productId,
            UUID variantId,
            String sku,
            String productNameSnapshot,
            String variantNameSnapshot,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal discountAmount,
            BigDecimal taxAmount,
            BigDecimal lineSubtotal,
            BigDecimal lineTotal,
            OffsetDateTime createdAt
    ) {
        this.id = Objects.requireNonNull(id, "Item ID cannot be null");
        this.orderId = Objects.requireNonNull(orderId, "Order ID cannot be null");
        this.productId = Objects.requireNonNull(productId, "Product ID cannot be null");
        this.variantId = variantId;
        this.sku = Objects.requireNonNull(sku, "SKU cannot be null");
        this.productNameSnapshot = Objects.requireNonNull(productNameSnapshot, "Product name snapshot cannot be null");
        this.variantNameSnapshot = variantNameSnapshot;
        this.unitPrice = Objects.requireNonNull(unitPrice, "Unit price cannot be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        this.quantity = quantity;
        this.discountAmount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        this.taxAmount = taxAmount != null ? taxAmount : BigDecimal.ZERO;
        this.lineSubtotal = Objects.requireNonNull(lineSubtotal, "Line subtotal cannot be null");
        this.lineTotal = Objects.requireNonNull(lineTotal, "Line total cannot be null");
        this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public UUID getProductId() { return productId; }
    public UUID getVariantId() { return variantId; }
    public String getSku() { return sku; }
    public String getProductNameSnapshot() { return productNameSnapshot; }
    public String getVariantNameSnapshot() { return variantNameSnapshot; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public BigDecimal getLineSubtotal() { return lineSubtotal; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem item = (OrderItem) o;
        return Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
