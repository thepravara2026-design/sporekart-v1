# SPOREKART v3.0 — INVENTORY FAILURE RECOVERY MATRIX

---

## Failure Recovery Specifications

| Scenario / Failure Event | Detection Mechanism | System Behavior | Recovery / Admin Action |
| :--- | :--- | :--- | :--- |
| **Concurrent High-Demand Reservation** | `findAllBySkuInOrderBySkuAscForUpdate` pessimistic lock | First transaction reserves stock; second transaction receives `InsufficientStockException`. | Second customer notified of item unavailability. |
| **Checkout Timeout / Abandonment** | `ReservationExpiryScheduler` batch poll | `ReservationStatus.ACTIVE` with `expiresAt < NOW()` automatically released. | Reserved stock restored to available pool via `releaseReservation`. |
| **Duplicate Checkout Request** | `reservationRepository.findByOrderId(orderId)` | Existing active reservation returned idempotently without double-reserving. | Safe idempotent response returned. |
| **Payment Failure** | Order / Payment failure event listener | Calls `releaseReservation(reservationId, "PAYMENT_FAILED")`. | Reserved stock released to available pool. |
| **Double Release Attempt** | `reservation.isActive()` check | Second release call logs info and returns existing `RELEASED` reservation. | Idempotent no-op. |
| **Double Commit Attempt** | `reservation.getStatus() == COMMITTED` check | Second commit call logs info and returns existing `COMMITTED` reservation. | Idempotent no-op. |
| **Return Restock Inspection** | `ReturnAcceptedEvent` emitted | If `ACCEPTED`, `adjustOnHand` increments `onHandQuantity` with `RESTOCK` movement. | Stock made available for purchase. |
| **Damaged Return Inspection** | Inspection outcome `DAMAGED` / `QUARANTINED` | Item added to `damagedQuantity` with `DAMAGE` movement. | Item excluded from available stock. |
