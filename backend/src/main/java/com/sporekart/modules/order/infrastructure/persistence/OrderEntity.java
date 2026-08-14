package com.sporekart.modules.order.infrastructure.persistence;

import com.sporekart.modules.order.domain.AddressSnapshot;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderItem;
import com.sporekart.modules.order.domain.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    private UUID id;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Column(name = "customer_id", nullable = false, length = 100)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "discount_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountTotal;

    @Column(name = "tax_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxTotal;

    @Column(name = "shipping_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingFee;

    @Column(name = "grand_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal grandTotal;

    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Column(name = "shipping_name", nullable = false, length = 200)
    private String shippingName;

    @Column(name = "shipping_phone", nullable = false, length = 50)
    private String shippingPhone;

    @Column(name = "shipping_address_line1", nullable = false, length = 255)
    private String shippingAddressLine1;

    @Column(name = "shipping_address_line2", length = 255)
    private String shippingAddressLine2;

    @Column(name = "shipping_city", nullable = false, length = 100)
    private String shippingCity;

    @Column(name = "shipping_state", nullable = false, length = 100)
    private String shippingState;

    @Column(name = "shipping_postal_code", nullable = false, length = 20)
    private String shippingPostalCode;

    @Column(name = "shipping_country", nullable = false, length = 100)
    private String shippingCountry;

    @Column(name = "customer_notes", length = 500)
    private String customerNotes;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public OrderEntity() {}

    public OrderEntity(
            UUID id,
            String orderNumber,
            String customerId,
            OrderStatus status,
            String currency,
            BigDecimal subtotal,
            BigDecimal discountTotal,
            BigDecimal taxTotal,
            BigDecimal shippingFee,
            BigDecimal grandTotal,
            String idempotencyKey,
            String shippingName,
            String shippingPhone,
            String shippingAddressLine1,
            String shippingAddressLine2,
            String shippingCity,
            String shippingState,
            String shippingPostalCode,
            String shippingCountry,
            String customerNotes,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.status = status;
        this.currency = currency;
        this.subtotal = subtotal;
        this.discountTotal = discountTotal;
        this.taxTotal = taxTotal;
        this.shippingFee = shippingFee;
        this.grandTotal = grandTotal;
        this.idempotencyKey = idempotencyKey;
        this.shippingName = shippingName;
        this.shippingPhone = shippingPhone;
        this.shippingAddressLine1 = shippingAddressLine1;
        this.shippingAddressLine2 = shippingAddressLine2;
        this.shippingCity = shippingCity;
        this.shippingState = shippingState;
        this.shippingPostalCode = shippingPostalCode;
        this.shippingCountry = shippingCountry;
        this.customerNotes = customerNotes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static OrderEntity fromDomain(Order order) {
        AddressSnapshot address = order.getShippingAddress();
        OrderEntity entity = new OrderEntity(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getStatus(),
                order.getCurrency(),
                order.getSubtotal(),
                order.getDiscountTotal(),
                order.getTaxTotal(),
                order.getShippingFee(),
                order.getGrandTotal(),
                order.getIdempotencyKey(),
                address.getFullName(),
                address.getPhone(),
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry(),
                order.getCustomerNotes(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );

        if (order.getItems() != null) {
            List<OrderItemEntity> itemEntities = order.getItems().stream()
                    .map(item -> OrderItemEntity.fromDomain(item, entity))
                    .toList();
            entity.setItems(itemEntities);
        }

        return entity;
    }

    public Order toDomain() {
        AddressSnapshot address = new AddressSnapshot(
                this.shippingName,
                this.shippingPhone,
                this.shippingAddressLine1,
                this.shippingAddressLine2,
                this.shippingCity,
                this.shippingState,
                this.shippingPostalCode,
                this.shippingCountry
        );

        List<OrderItem> domainItems = this.items != null
                ? this.items.stream().map(OrderItemEntity::toDomain).toList()
                : new ArrayList<>();

        return new Order(
                this.id,
                this.orderNumber,
                this.customerId,
                this.status,
                this.currency,
                this.subtotal,
                this.discountTotal,
                this.taxTotal,
                this.shippingFee,
                this.grandTotal,
                this.idempotencyKey,
                address,
                this.customerNotes,
                domainItems,
                this.createdAt,
                this.updatedAt
        );
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscountTotal() { return discountTotal; }
    public void setDiscountTotal(BigDecimal discountTotal) { this.discountTotal = discountTotal; }

    public BigDecimal getTaxTotal() { return taxTotal; }
    public void setTaxTotal(BigDecimal taxTotal) { this.taxTotal = taxTotal; }

    public BigDecimal getShippingFee() { return shippingFee; }
    public void setShippingFee(BigDecimal shippingFee) { this.shippingFee = shippingFee; }

    public BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getShippingName() { return shippingName; }
    public void setShippingName(String shippingName) { this.shippingName = shippingName; }

    public String getShippingPhone() { return shippingPhone; }
    public void setShippingPhone(String shippingPhone) { this.shippingPhone = shippingPhone; }

    public String getShippingAddressLine1() { return shippingAddressLine1; }
    public void setShippingAddressLine1(String shippingAddressLine1) { this.shippingAddressLine1 = shippingAddressLine1; }

    public String getShippingAddressLine2() { return shippingAddressLine2; }
    public void setShippingAddressLine2(String shippingAddressLine2) { this.shippingAddressLine2 = shippingAddressLine2; }

    public String getShippingCity() { return shippingCity; }
    public void setShippingCity(String shippingCity) { this.shippingCity = shippingCity; }

    public String getShippingState() { return shippingState; }
    public void setShippingState(String shippingState) { this.shippingState = shippingState; }

    public String getShippingPostalCode() { return shippingPostalCode; }
    public void setShippingPostalCode(String shippingPostalCode) { this.shippingPostalCode = shippingPostalCode; }

    public String getShippingCountry() { return shippingCountry; }
    public void setShippingCountry(String shippingCountry) { this.shippingCountry = shippingCountry; }

    public String getCustomerNotes() { return customerNotes; }
    public void setCustomerNotes(String customerNotes) { this.customerNotes = customerNotes; }

    public List<OrderItemEntity> getItems() { return items; }
    public void setItems(List<OrderItemEntity> items) {
        this.items.clear();
        if (items != null) {
            items.forEach(item -> item.setOrder(this));
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
        OrderEntity that = (OrderEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
