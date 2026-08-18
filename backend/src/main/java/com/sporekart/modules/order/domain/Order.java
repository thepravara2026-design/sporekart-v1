package com.sporekart.modules.order.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class Order {

    private final UUID id;
    private final String orderNumber;
    private final String customerId;
    private OrderStatus status;
    private final String currency;
    private final BigDecimal subtotal;
    private final BigDecimal discountTotal;
    private final BigDecimal taxTotal;
    private final BigDecimal shippingFee;
    private final BigDecimal grandTotal;
    private final String idempotencyKey;
    private final AddressSnapshot shippingAddress;
    private final String customerNotes;
    private String growerId;
    private final List<OrderItem> items;
    private Long version;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Order(
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
            AddressSnapshot shippingAddress,
            String customerNotes,
            String growerId,
            List<OrderItem> items,
            Long version,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "Order ID cannot be null");
        this.orderNumber = Objects.requireNonNull(orderNumber, "Order number cannot be null");
        this.customerId = Objects.requireNonNull(customerId, "Customer ID cannot be null");
        this.status = Objects.requireNonNull(status, "Order status cannot be null");
        this.currency = currency != null ? currency : "INR";
        this.subtotal = Objects.requireNonNull(subtotal, "Subtotal cannot be null");
        this.discountTotal = discountTotal != null ? discountTotal : BigDecimal.ZERO;
        this.taxTotal = taxTotal != null ? taxTotal : BigDecimal.ZERO;
        this.shippingFee = shippingFee != null ? shippingFee : BigDecimal.ZERO;
        this.grandTotal = Objects.requireNonNull(grandTotal, "Grand total cannot be null");
        this.idempotencyKey = idempotencyKey;
        this.shippingAddress = Objects.requireNonNull(shippingAddress, "Shipping address snapshot cannot be null");
        this.customerNotes = customerNotes;
        this.growerId = growerId;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.version = version != null ? version : 0L;
        this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : OffsetDateTime.now();
    }

    public Order(
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
            AddressSnapshot shippingAddress,
            String customerNotes,
            List<OrderItem> items,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this(id, orderNumber, customerId, status, currency, subtotal, discountTotal, taxTotal, shippingFee, grandTotal, idempotencyKey, shippingAddress, customerNotes, items, 0L, createdAt, updatedAt);
    }

    public static Order createNewOrder(
            String orderNumber,
            String customerId,
            String currency,
            BigDecimal subtotal,
            BigDecimal discountTotal,
            BigDecimal taxTotal,
            BigDecimal shippingFee,
            BigDecimal grandTotal,
            String idempotencyKey,
            AddressSnapshot shippingAddress,
            String customerNotes,
            List<OrderItem> items
    ) {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        return new Order(
                id, orderNumber, customerId, OrderStatus.CREATED,
                currency, subtotal, discountTotal, taxTotal, shippingFee, grandTotal,
                idempotencyKey, shippingAddress, customerNotes, items, 0L, now, now
        );
    }

    public void transitionToPaymentPending() {
        changeStatus(OrderStatus.PAYMENT_PENDING);
    }

    public void markPaid() {
        changeStatus(OrderStatus.PAID);
    }

    public void markConfirmed() {
        changeStatus(OrderStatus.CONFIRMED);
    }

    public void markPaymentFailed(String reason) {
        changeStatus(OrderStatus.PAYMENT_FAILED);
    }

    public void startProcessing() {
        changeStatus(OrderStatus.PROCESSING);
    }

    public void markReadyForFulfilment() {
        changeStatus(OrderStatus.READY_FOR_FULFILMENT);
    }

    public void markShipped() {
        changeStatus(OrderStatus.SHIPPED);
    }

    public void markOutForDelivery() {
        changeStatus(OrderStatus.OUT_FOR_DELIVERY);
    }

    public void markDelivered() {
        changeStatus(OrderStatus.DELIVERED);
    }

    public void markCompleted() {
        changeStatus(OrderStatus.COMPLETED);
    }

    public void cancel() {
        changeStatus(OrderStatus.CANCELLED);
    }

    public void expire() {
        changeStatus(OrderStatus.EXPIRED);
    }

    private void changeStatus(OrderStatus targetStatus) {
        if (this.status == targetStatus) {
            return; // Idempotent
        }
        OrderStateMachine.validateTransition(this.id, this.status, targetStatus);
        this.status = targetStatus;
        this.updatedAt = OffsetDateTime.now();
    }

    public boolean isCancellable() {
        return this.status.isCancellable();
    }

    public boolean isTerminal() {
        return this.status.isTerminal();
    }

    // Getters
    public UUID getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public String getCustomerId() { return customerId; }
    public OrderStatus getStatus() { return status; }
    public String getCurrency() { return currency; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDiscountTotal() { return discountTotal; }
    public BigDecimal getTaxTotal() { return taxTotal; }
    public BigDecimal getShippingFee() { return shippingFee; }
    public BigDecimal getGrandTotal() { return grandTotal; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public AddressSnapshot getShippingAddress() { return shippingAddress; }
    public String getCustomerNotes() { return customerNotes; }
    public String getGrowerId() { return growerId; }
    public void setGrowerId(String growerId) { this.growerId = growerId; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public Long getVersion() { return version; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
