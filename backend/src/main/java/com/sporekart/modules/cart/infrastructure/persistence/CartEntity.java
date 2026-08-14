package com.sporekart.modules.cart.infrastructure.persistence;

import com.sporekart.modules.cart.domain.Cart;
import com.sporekart.modules.cart.domain.CartItem;
import com.sporekart.modules.cart.domain.CartStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.springframework.data.domain.Persistable;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Transient;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "carts")
public class CartEntity {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false, length = 100)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CartStatus status;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "item_count", nullable = false)
    private int itemCount;

    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItemEntity> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public CartEntity() {}

    public CartEntity(
            UUID id,
            String customerId,
            CartStatus status,
            String currency,
            BigDecimal subtotal,
            int itemCount,
            Long version,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.currency = currency;
        this.subtotal = subtotal;
        this.itemCount = itemCount;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CartEntity fromDomain(Cart cart) {
        CartEntity entity = new CartEntity(
                cart.getId(),
                cart.getCustomerId(),
                cart.getStatus(),
                cart.getCurrency(),
                cart.getSubtotal(),
                cart.getItemCount(),
                cart.getVersion(),
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );

        if (cart.getItems() != null) {
            List<CartItemEntity> itemEntities = cart.getItems().stream()
                    .map(item -> CartItemEntity.fromDomain(item, entity))
                    .toList();
            entity.setItems(itemEntities);
        }

        return entity;
    }

    public Cart toDomain() {
        List<CartItem> domainItems = this.items != null
                ? this.items.stream().map(CartItemEntity::toDomain).toList()
                : new ArrayList<>();

        return new Cart(
                this.id,
                this.customerId,
                this.status,
                this.currency,
                this.subtotal,
                this.itemCount,
                this.version,
                domainItems,
                this.createdAt,
                this.updatedAt
        );
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public CartStatus getStatus() { return status; }
    public void setStatus(CartStatus status) { this.status = status; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public List<CartItemEntity> getItems() { return items; }
    public void setItems(List<CartItemEntity> items) {
        this.items.clear();
        if (items != null) {
            items.forEach(item -> item.setCart(this));
            this.items.addAll(items);
        }
    }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartEntity that = (CartEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
