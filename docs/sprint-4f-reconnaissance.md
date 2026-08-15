# SPOREKART v3.0 — SPRINT 4F RECONNAISSANCE & ARCHITECTURE ANALYSIS

---

## 1. Executive Summary

Sprint 4F focuses on **Shipping, Delivery Orchestration & Carrier Abstraction**.
This document synthesizes our reconnaissance of the existing Sporekart codebase across Sprint 0 through Sprint 4E, outlining the exact shipment aggregate (`Shipment`), provider abstraction (`ShippingProvider`), Shiprocket adapter (`ShiprocketShippingProvider`), mock provider adapter (`MockShippingProvider`), Flyway database schemas (`V9`), webhook verification & idempotency, tracking event ledgers, and shipping reconciliation.

---

## 2. Existing Codebase Reconnaissance & Findings

### 2.1 Database Schema (`V9__shipping_domain.sql`)
The shipping module comprises five database tables:
- `shipments`: `id`, `shipment_reference`, `order_id`, `order_reference`, `customer_id`, `status`, `provider`, `provider_shipment_id`, `awb`, `tracking_number`, `courier_name`, `courier_code`, `weight_grams`, `declared_value`, `shipping_name`, `shipping_phone`, `shipping_address_line1-line2-city-state-postal_code-country`, `estimated_delivery_at`, `booked_at`, `picked_up_at`, `delivered_at`.
- `shipment_items`: `id`, `shipment_id`, `order_item_id`, `product_id`, `sku`, `product_name_snapshot`, `quantity`.
- `shipment_status_history`: `id`, `shipment_id`, `previous_status`, `new_status`, `reason`, `actor_type`, `actor_id`, `provider_event_id`, `correlation_id`.
- `shipment_tracking_events`: `id`, `shipment_id`, `provider_event_id`, `provider_status`, `normalized_status`, `description`, `location`, `occurred_at`, with constraint `CONSTRAINT uq_shipment_provider_event UNIQUE (shipment_id, provider_event_id)`.
- `shipping_webhook_events`: `id`, `provider`, `provider_event_id`, `event_type`, `payload`, `processed_at`, with constraint `CONSTRAINT uq_shipping_provider_webhook UNIQUE (provider, provider_event_id)`.

### 2.2 Domain Boundaries & Integration Points
- **Order Domain (`com.sporekart.modules.order`)**: `OrderLifecycleEventListener` listens for `OrderLifecycleEvent` when order reaches `READY_FOR_FULFILMENT` to trigger `createShipmentForOrder(orderId)`.
- **Shipping Domain (`com.sporekart.modules.shipment`)**: Authoritative for carrier selection, AWB booking, shipping address snapshots (`ShippingAddressSnapshot`), provider abstraction (`ShippingProvider`), webhook signature verification, tracking history, and delivery state transitions (`CREATED` -> `READY_FOR_BOOKING` -> `BOOKING_PENDING` -> `BOOKED` -> `PICKUP_SCHEDULED` -> `PICKED_UP` -> `IN_TRANSIT` -> `OUT_FOR_DELIVERY` -> `DELIVERED`).
- **Inventory Domain (`com.sporekart.modules.inventory`)**: `InventoryOrderEventListener` listens for `OrderLifecycleEvent` when shipment transitions to `SHIPPED` / `DELIVERED` to commit reserved stock (`commitReservation`).
- **Return Domain (`com.sporekart.modules.returns`)**: `ShipmentStatusEventListener` listens to `ShipmentLifecycleEvent` updates for reverse logistics.

---

## 3. Provider Abstraction Architecture

```
                        ORDER DOMAIN
                             |
                             v
                      SHIPPING DOMAIN
                  (Shipment Aggregate Root)
                             |
                   SHIPPING PROVIDER REGISTRY
                             |
        +--------------------+--------------------+
        |                                         |
        v                                         v
SHIPROCKET ADAPTER                        MOCK ADAPTER
 (Production/Staging)                      (DEV/QA Test)
```

---

## 4. Identified Architectural Enhancements for Sprint 4F

1. **Carrier Selection Strategy (`CarrierSelectionPort`)**:
   - Implement `CarrierSelectionPort` and `DefaultCarrierSelectionStrategy` supporting dynamic provider selection based on order attributes or provider preference.
2. **Label & Manifest SPI Methods**:
   - Add `getLabelUrl` and `getManifestUrl` capability methods to `ShippingProvider` SPI.
3. **Single Shipment Sync Endpoint**:
   - Add `POST /api/v1/admin/shipments/{shipmentReference}/sync` to `AdminShipmentController` and `syncShipmentWithProvider` in `ShipmentApplicationService` to poll single active shipments out-of-band.
4. **Label & Manifest Endpoints**:
   - Expose `GET /api/v1/admin/shipments/{shipmentReference}/label` and `GET /api/v1/admin/shipments/{shipmentReference}/manifest` in `AdminShipmentController`.
5. **Frontend API (`shippingApi.ts`)**:
   - Add `shippingApi.ts` in `frontend/src/services/` for customer tracking and admin shipment management.

---

## 5. Verification & Safety Plan

- All Flyway migrations (`V1` through `V13`) run deterministically.
- Full verification across backend (`mvn clean test`), frontend unit tests (`npm run test -- --run`), and production build (`npm run build`).
