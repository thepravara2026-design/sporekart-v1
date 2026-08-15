# SPOREKART v3.0 — RETURNS & REFUNDS ARCHITECTURE

---

## 1. Subsystem Architecture

```
                                 ORDER DOMAIN
                                      |
         +----------------------------+----------------------------+
         |                                                         |
         v                                                         v
  PAYMENT DOMAIN                                            SHIPMENT DOMAIN
  (Payment / Razorpay)                                   (ShippingProvider SPI)
         |                                                         |
         +----------------------------+----------------------------+
                                      |
                                      v
                                RETURN DOMAIN
                                      |
         +----------------------------+----------------------------+
         |                            |                            |
         v                            v                            v
  REVERSE LOGISTICS              INSPECTION                REFUND ORCHESTRATION
  (Reverse Shipment)        (Warehouse QA)            (PaymentProvider Refund)
```

---

## 2. Invariants & Financial Protection

1. **Item Quantity Limits**: Cumulative requested return quantity for an order item across all active return requests cannot exceed the purchased quantity.
2. **Refund Cap**: Cumulative processed refunds for an order cannot exceed the order total amount.
3. **Idempotency**: All payment provider refund requests use deterministic idempotency key format `RFD-{returnReference}`.
4. **Inventory Restock**: Accepted return items emit `ReturnAcceptedEvent` to increment available inventory via `InventoryApplicationService` without coupling the return domain to internal inventory tables.
