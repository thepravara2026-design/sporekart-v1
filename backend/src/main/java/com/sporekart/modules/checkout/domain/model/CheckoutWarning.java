package com.sporekart.modules.checkout.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class CheckoutWarning {

    public enum WarningType {
        PRICE_CHANGED,
        ITEM_UNAVAILABLE,
        STOCK_LIMITED,
        GENERAL
    }

    private final WarningType type;
    private final UUID productId;
    private final String message;

    public CheckoutWarning(WarningType type, UUID productId, String message) {
        this.type = Objects.requireNonNull(type, "Warning type cannot be null");
        this.productId = productId;
        this.message = Objects.requireNonNull(message, "Warning message cannot be null");
    }

    public WarningType getType() { return type; }
    public UUID getProductId() { return productId; }
    public String getMessage() { return message; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CheckoutWarning warning = (CheckoutWarning) o;
        return type == warning.type
                && Objects.equals(productId, warning.productId)
                && Objects.equals(message, warning.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, productId, message);
    }
}
