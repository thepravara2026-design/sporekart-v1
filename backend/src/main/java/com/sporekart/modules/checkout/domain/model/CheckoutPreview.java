package com.sporekart.modules.checkout.domain.model;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class CheckoutPreview {

    private final UUID previewId;
    private final UUID cartId;
    private final String customerId;
    private final String currency;
    private final List<CheckoutLineItem> items;
    private final Money subtotal;
    private final Money discountTotal;
    private final Money taxTotal;
    private final Money shippingFee;
    private final Money grandTotal;
    private final List<CheckoutWarning> warnings;
    private final OffsetDateTime generatedAt;

    public CheckoutPreview(
            UUID previewId,
            UUID cartId,
            String customerId,
            String currency,
            List<CheckoutLineItem> items,
            Money subtotal,
            Money discountTotal,
            Money taxTotal,
            Money shippingFee,
            Money grandTotal,
            List<CheckoutWarning> warnings,
            OffsetDateTime generatedAt
    ) {
        this.previewId = Objects.requireNonNull(previewId, "Preview ID cannot be null");
        this.cartId = Objects.requireNonNull(cartId, "Cart ID cannot be null");
        this.customerId = Objects.requireNonNull(customerId, "Customer ID cannot be null");
        this.currency = Objects.requireNonNull(currency, "Currency cannot be null");
        this.items = List.copyOf(Objects.requireNonNull(items, "Items list cannot be null"));
        this.subtotal = Objects.requireNonNull(subtotal, "Subtotal cannot be null");
        this.discountTotal = Objects.requireNonNull(discountTotal, "Discount total cannot be null");
        this.taxTotal = Objects.requireNonNull(taxTotal, "Tax total cannot be null");
        this.shippingFee = Objects.requireNonNull(shippingFee, "Shipping fee cannot be null");
        this.grandTotal = Objects.requireNonNull(grandTotal, "Grand total cannot be null");
        this.warnings = warnings != null ? List.copyOf(warnings) : Collections.emptyList();
        this.generatedAt = Objects.requireNonNull(generatedAt, "Generated timestamp cannot be null");
    }

    public UUID getPreviewId() { return previewId; }
    public UUID getCartId() { return cartId; }
    public String getCustomerId() { return customerId; }
    public String getCurrency() { return currency; }
    public List<CheckoutLineItem> getItems() { return items; }
    public Money getSubtotal() { return subtotal; }
    public Money getDiscountTotal() { return discountTotal; }
    public Money getTaxTotal() { return taxTotal; }
    public Money getShippingFee() { return shippingFee; }
    public Money getGrandTotal() { return grandTotal; }
    public List<CheckoutWarning> getWarnings() { return warnings; }
    public OffsetDateTime getGeneratedAt() { return generatedAt; }
    public boolean hasWarnings() { return !warnings.isEmpty(); }
}
