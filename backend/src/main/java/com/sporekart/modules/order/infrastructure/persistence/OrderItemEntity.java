package com.sporekart.modules.order.infrastructure.persistence;

import com.sporekart.modules.order.domain.OrderItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItemEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "variant_id")
    private UUID variantId;

    @Column(name = "sku", nullable = false, length = 100)
    private String sku;

    @Column(name = "product_name_snapshot", nullable = false, length = 255)
    private String productNameSnapshot;

    @Column(name = "variant_name_snapshot", length = 255)
    private String variantNameSnapshot;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "line_subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineSubtotal;

    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public OrderItemEntity() {}

    public OrderItemEntity(
            UUID id,
            OrderEntity order,
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
        this.id = id;
        this.order = order;
        this.productId = productId;
        this.variantId = variantId;
        this.sku = sku;
        this.productNameSnapshot = productNameSnapshot;
        this.variantNameSnapshot = variantNameSnapshot;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.discountAmount = discountAmount;
        this.taxAmount = taxAmount;
        this.lineSubtotal = lineSubtotal;
        this.lineTotal = lineTotal;
        this.createdAt = createdAt;
    }

    public static OrderItemEntity fromDomain(OrderItem item, OrderEntity orderEntity) {
        return new OrderItemEntity(
                item.getId(),
                orderEntity,
                item.getProductId(),
                item.getVariantId(),
                item.getSku(),
                item.getProductNameSnapshot(),
                item.getVariantNameSnapshot(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getDiscountAmount(),
                item.getTaxAmount(),
                item.getLineSubtotal(),
                item.getLineTotal(),
                item.getCreatedAt()
        );
    }

    public OrderItem toDomain() {
        return new OrderItem(
                this.id,
                this.order != null ? this.order.getId() : null,
                this.productId,
                this.variantId,
                this.sku,
                this.productNameSnapshot,
                this.variantNameSnapshot,
                this.unitPrice,
                this.quantity,
                this.discountAmount,
                this.taxAmount,
                this.lineSubtotal,
                this.lineTotal,
                this.createdAt
        );
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public OrderEntity getOrder() { return order; }
    public void setOrder(OrderEntity order) { this.order = order; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public UUID getVariantId() { return variantId; }
    public void setVariantId(UUID variantId) { this.variantId = variantId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getProductNameSnapshot() { return productNameSnapshot; }
    public void setProductNameSnapshot(String productNameSnapshot) { this.productNameSnapshot = productNameSnapshot; }

    public String getVariantNameSnapshot() { return variantNameSnapshot; }
    public void setVariantNameSnapshot(String variantNameSnapshot) { this.variantNameSnapshot = variantNameSnapshot; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getLineSubtotal() { return lineSubtotal; }
    public void setLineSubtotal(BigDecimal lineSubtotal) { this.lineSubtotal = lineSubtotal; }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItemEntity that = (OrderItemEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
