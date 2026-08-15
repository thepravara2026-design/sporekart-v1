# SPOREKART v3.0 — REPLACEMENT WORKFLOW SPECIFICATION

---

## 1. Replacement Request Lifecycle

```
    Replacement Requested (RPL-2026-XXXXXX)
                      ↓
    Check Inventory Availability (SKU)
                      ↓
       +--------------┴--------------+
       |                             |
  Stock Available             Out of Stock
       ↓                             ↓
  Create Reservation            Reject / Refund
       ↓
  Approve Replacement
       ↓
  Book Replacement Shipment
       ↓
  Deliver Replacement
       ↓
  Resolve Ticket
```

---

## 2. Cross-Domain Integrations

- **Inventory**: Calls `InventoryApplicationService.getInventoryBySku(sku)` and `InventoryApplicationService.createReservation(...)`.
- **Shipping**: Calls `ShipmentApplicationService.createShipmentForOrder(orderId)` or books replacement shipment reference.
- **Support**: Tracks replacement request aggregate (`ReplacementRequest`) linked to `SupportTicket`.
