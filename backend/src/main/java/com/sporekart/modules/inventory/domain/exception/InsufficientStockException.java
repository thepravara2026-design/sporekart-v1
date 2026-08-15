package com.sporekart.modules.inventory.domain.exception;

public class InsufficientStockException extends RuntimeException {

    private final String sku;
    private final int requestedQuantity;
    private final int availableQuantity;

    public InsufficientStockException(String sku, int requestedQuantity, int availableQuantity) {
        super("Insufficient stock for SKU: " + sku + ". Requested: " + requestedQuantity + ", Available: " + availableQuantity);
        this.sku = sku;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public String getSku() { return sku; }
    public int getRequestedQuantity() { return requestedQuantity; }
    public int getAvailableQuantity() { return availableQuantity; }
}
