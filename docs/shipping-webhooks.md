# SPOREKART v3.0 — SHIPPING WEBHOOK SPECIFICATION

---

## 1. Webhook Endpoint Architecture

Endpoint: `POST /api/v1/webhooks/shipping/{provider}`

Workflow:
1. **Signature Verification**: `ShippingProvider.verifyWebhookSignature(rawBody, headers)` validates authenticity.
2. **Deduplication Check**: Database check `existsWebhookEvent(provider, providerEventId)` and UNIQUE constraint prevent duplicate processing.
3. **Event Normalization**: Parses raw payload into `NormalizedWebhookEvent`.
4. **State Machine Update**: Appends checkpoint to `shipment_tracking_events` and updates `shipments` status if status progressed.
5. **Order Domain Event Sync**: Triggers order status updates (`markShipped`, `markOutForDelivery`, `markDelivered`) cleanly.
