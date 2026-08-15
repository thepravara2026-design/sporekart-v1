# SPOREKART v3.0 — SPRINT 4F IMPLEMENTATION DETAILS

---

### 1. Architectural Overview

Sprint 4F establishes a production-grade Shipping & Delivery Orchestration subsystem (`com.sporekart.modules.shipment`) supporting:
- **Provider Abstraction (`ShippingProvider`)**: Provider interface separating domain models from provider SDKs (Shiprocket, Mock).
- **Dynamic Carrier Selection (`CarrierSelectionPort`)**: Carrier selection strategy (`DefaultCarrierSelectionStrategy`) routing requests dynamically based on destination postal code, package weight, order total, and payment mode.
- **Immutable Address Snapshot (`ShippingAddressSnapshot`)**: Immutable recipient snapshot captured at shipment creation time.
- **Append-Only Tracking Ledger (`shipment_tracking_events`)**: Deduplicated event tracking history (`uq_shipment_provider_event`).
- **Webhook Processing & Idempotency (`ShippingWebhookController`)**: Signature verification (`verifyWebhookSignature`), event normalization, deduplication (`uq_shipping_provider_webhook`), and order lifecycle synchronization.
- **Reconciliation Engine (`reconcileActiveShipments` & `syncShipmentWithProvider`)**: Automated batch polling and single-shipment manual synchronization.
- **REST APIs**: Customer tracking (`GET /api/v1/orders/{orderRef}/shipment`) and Admin shipment management (`GET /api/v1/admin/shipments`, `POST /{ref}/retry`, `POST /{ref}/sync`, `POST /{ref}/cancel`, `GET /{ref}/label`, `GET /{ref}/manifest`).

---

### 2. File Changes Summary

#### Backend (`com.sporekart.modules.shipment`)
- `CarrierSelectionPort.java`: Port interface for dynamic carrier selection.
- `DefaultCarrierSelectionStrategy.java`: Strategy implementation selecting configured provider.
- `ShippingProvider.java`: Added `getLabelUrl` and `getManifestUrl` SPI methods.
- `ShiprocketShippingProvider.java` & `MockShippingProvider.java`: Implement `getLabelUrl` and `getManifestUrl`.
- `ShipmentApplicationService.java`: Added `syncShipmentWithProvider`, `getShipmentLabelUrl`, and `getShipmentManifestUrl`.
- `AdminShipmentController.java`: Added `POST /{ref}/sync`, `GET /{ref}/label`, `GET /{ref}/manifest` REST endpoints.

#### Frontend (`frontend/src/`)
- `endpoints.ts`: Added shipment endpoints.
- `shippingApi.ts`: Axios API service for customer tracking and admin shipment management.
