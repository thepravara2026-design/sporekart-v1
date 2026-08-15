# SPOREKART v3.0 — SHIPROCKET INTEGRATION & TOKEN MANAGEMENT

---

## 1. Shiprocket Provider Overview

The `ShiprocketShippingProvider` implements the provider SPI boundary (`ShippingProvider`) to interface with Shiprocket API v2 (`https://apiv2.shiprocket.in/v1/external`).

---

## 2. Configuration Properties

```properties
# Shipping Provider Selection (MOCK | SHIPROCKET)
sporekart.shipping.provider=MOCK

# Shiprocket Provider Configuration
sporekart.shipping.shiprocket.api-url=https://apiv2.shiprocket.in/v1/external
sporekart.shipping.shiprocket.email=demo@sporekart.com
sporekart.shipping.shiprocket.password=demo123
sporekart.shipping.shiprocket.webhook-token=shiprocket_secret_token_123
```

---

## 3. Token Management & Security Invariants

1. **Authentication Token Lifecycle**:
   - `getAuthToken()` handles JWT authentication token retrieval.
   - Tokens are cached in-memory with automatic expiration tracking (valid for 9 days per Shiprocket specification).
   - Thread-safe synchronization ensures parallel booking calls do not trigger duplicate login API requests.
2. **Webhook Verification**:
   - Webhooks are authenticated by comparing the `x-api-key` or `x-shiprocket-token` header against `sporekart.shipping.shiprocket.webhook-token`.
3. **Status Mapping**:
   - Raw Shiprocket statuses (`DELIVERED`, `OUT FOR DELIVERY`, `PICKED UP`, `IN TRANSIT`, `RTO`, `UNDELIVERED`, `CANCELED`) are mapped strictly into canonical `ShipmentStatus` enum values.
