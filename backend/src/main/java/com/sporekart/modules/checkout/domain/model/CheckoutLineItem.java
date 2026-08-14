package com.sporekart.modules.checkout.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class CheckoutLineItem {

    private final UUID cartItemId;
    private final UUID productId;
    private final String sku;
    private final String productName;
    private final int quantity;
    private final Money cartUnitPrice;
    private final Money authoritativeUnitPrice;
    private final boolean priceChanged;
    private final Money lineSubtotal;
    private final Money discountAmount;
    private final Money taxAmount;
    private final Money lineTotal;

    public CheckoutLineItem(
            UUID cartItemId,
            UUID productId,
            String sku,
            String productName,
            int quantity,
            Money cartUnitPrice,
            Money authoritativeUnitPrice,
            boolean priceChanged,
            Money lineSubtotal,
            Money discountAmount,
            Money taxAmount,
            Money lineTotal
    ) {
        this.cartItemId = cartItemId;
        this.productId = Objects.requireNonNull(productId, "Product ID cannot be null");
        this.sku = Objects.requireNonNull(sku, "SKU cannot be null");
        this.productName = Objects.requireNonNull(productName, "Product name cannot be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        this.quantity = quantity;
        this.cartUnitPrice = Objects.requireNonNull(cartUnitPrice, "Cart unit price cannot be null");
        this.authoritativeUnitPrice = Objects.requireNonNull(authoritativeUnitPrice, "Authoritative unit price cannot be null");
        this.priceChanged = priceChanged;
        this.lineSubtotal = Objects.requireNonNull(lineSubtotal, "Line subtotal cannot be null");
        this.discountAmount = Objects.requireNonNull(discountAmount, "Discount amount cannot be null");
        this.taxAmount = Objects.requireNonNull(taxAmount, "Tax amount cannot be null");
        this.lineTotal = Objects.requireNonNull(lineTotal, "Line total cannot be null");
    }

    public UUID getCartItemId() { return cartItemId; }
    public UUID getProductId() { return productId; }
    public String getSku() { return sku; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public Money getCartUnitPrice() { return cartUnitPrice; }
    public Money getAuthoritativeUnitPrice() { return authoritativeUnitPrice; }
    public boolean isPriceChanged() { return priceChanged; }
    public Money getLineSubtotal() { return lineSubtotal; }
    public Money getDiscountAmount() { return discountAmount; }
    public Money getTaxAmount() { return taxAmount; }
    public Money getLineTotal() { return lineTotal; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CheckoutLineItem item = (CheckoutLineItem) o;
        return quantity == item.quantity
                && priceChanged == item.priceChanged
                && Objects.equals(cartItemId, item.cartItemId)
                && Objects.equals(productId, item.productId)
                && Objects.equals(sku, item.sku)
                && Objects.equals(productName, item.productName)
                && Objects.equals(cartUnitPrice, item.cartUnitPrice)
                && Objects.equals(authoritativeUnitPrice, item.authoritativeUnitPrice)
                && Objects.equals(lineSubtotal, item.lineSubtotal)
                && Objects.equals(discountAmount, item.discountAmount)
                && Objects.equals(taxAmount, item.taxAmount)
                && Objects.equals(lineTotal, item.lineTotal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                cartItemId, productId, sku, productName, quantity,
                cartUnitPrice, authoritativeUnitPrice, priceChanged,
                lineSubtotal, discountAmount, taxAmount, lineTotal
        );
    }
}
