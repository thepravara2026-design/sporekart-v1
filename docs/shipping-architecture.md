# SPOREKART v3.0 — SHIPPING & DELIVERY ORCHESTRATION ARCHITECTURE

---

## 1. Subsystem Architecture

```
                          ORDER DOMAIN
                               |
                               v
                        SHIPPING DOMAIN
                   (Shipment Aggregate Root)
                               |
       +-----------------------+-----------------------+
       |                       |                       |
       v                       v                       v
CARRIER REGISTRY         TRACKING HISTORY       RECONCILIATION
 (Shiprocket /           (Append-Only Event     (Provider Polling
   Mock Provider)             Ledger)              & Sync Engine)
```

---

## 2. Domain Invariants

1. **Provider Isolation**: External provider SDKs and DTOs (e.g. Shiprocket) are strictly isolated inside provider adapters and never exposed to the Order domain or customer APIs.
2. **Immutable Address Snapshot**: `ShippingAddressSnapshot` is created at shipment creation time and preserved independently of subsequent customer profile changes.
3. **Webhook Idempotency**: Webhook events are verified via provider signatures and deduplicated via `CONSTRAINT uq_shipping_provider_webhook UNIQUE (provider, provider_event_id)`.
4. **Non-Regressive Transitions**: Shipment state transitions follow strict state machine rules (`ShipmentStateMachine.java`).
