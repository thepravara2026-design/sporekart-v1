package com.sporekart.modules.catalog.domain.product;

public enum QuantityUnit {
    G("g", "Grams"),
    KG("kg", "Kilograms"),
    ML("ml", "Milliliters"),
    L("l", "Liters");

    private final String symbol;
    private final String displayName;

    QuantityUnit(String symbol, String displayName) {
        this.symbol = symbol;
        this.displayName = displayName;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static QuantityUnit parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Quantity unit cannot be null or blank");
        }
        String normalized = raw.trim().toUpperCase();
        for (QuantityUnit unit : values()) {
            if (unit.name().equals(normalized) || unit.symbol.equalsIgnoreCase(normalized)) {
                return unit;
            }
        }
        throw new IllegalArgumentException("Unsupported quantity unit: '" + raw + "'. Supported units are: G, KG, ML, L");
    }
}
