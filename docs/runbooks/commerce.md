# SPOREKART v3.0 — Commerce Operations Runbook

## Emergency Escalation & Operational Playbook

---

### 1. Payment Verification / Webhook Failures

#### Symptoms
- Orders remain stuck in `CREATED` or `PAYMENT_PENDING` status despite successful customer payment.
- Customers report payment charged on provider portal but order not confirmed in Sporekart.

#### Diagnosis Procedure
1. Search application logs for `PaymentApplicationService` or `PaymentWebhookEventListener`.
2. Inspect log output for `SignatureVerificationException` or `PaymentAttemptNotFound`.
3. Check `payment_webhook_events` table in PostgreSQL:
   ```sql
   SELECT * FROM payment_webhook_events WHERE processing_status = 'FAILED' ORDER BY received_at DESC LIMIT 10;
   ```

#### Safe Action
- Manually trigger payment verification retry using payment reference ID:
  ```bash
  curl -X POST http://localhost:8080/api/v1/payments/verify \
    -H "Content-Type: application/json" \
    -d '{"paymentReference": "PAY-SPK-XXXXX", "providerPaymentId": "pay_mock_123"}'
  ```

---

### 2. Inventory Reservation Discrepancies & Stuck Locks

#### Symptoms
- Customers receive `InsufficientStockException` when catalog shows available stock.
- Outdated stock reservation holds inventory past TTL expiration window.

#### Diagnosis Procedure
1. Check `stock_reservations` table for expired active reservations:
   ```sql
   SELECT * FROM stock_reservations WHERE status = 'ACTIVE' AND expires_at < NOW();
   ```
2. Verify total on-hand vs reserved quantities in `inventory_items`:
   ```sql
   SELECT sku, on_hand_quantity, reserved_quantity FROM inventory_items WHERE reserved_quantity > on_hand_quantity;
   ```

#### Safe Action
- Run automated inventory cleanup batch or execute explicit reservation release via `InventoryApplicationService.releaseReservation(orderId, "OPERATIONAL_EXPIRATION")`.

---

### 3. Outbox Event Backlog / Stuck Async Events

#### Symptoms
- Shipments or returns are not transitioning automatically when order state changes.
- Outbox message table count accumulating.

#### Diagnosis Procedure
1. Query outbox event table:
   ```sql
   SELECT count(*) FROM outbox_events WHERE status = 'PENDING' OR status = 'FAILED';
   ```

#### Safe Action
- Restart background event listener pool or invoke outbox processor retry endpoint.

---

### 4. Database Connection Pool Exhaustion

#### Symptoms
- Log output displays `HikariPool-1 - Connection is not available, request timed out after 30000ms`.

#### Diagnosis Procedure
1. Inspect active database connections in PostgreSQL:
   ```sql
   SELECT count(*), state FROM pg_stat_activity GROUP BY state;
   ```
2. Verify HikariCP max pool size setting in `application-prod.yml` (`maximum-pool-size: 20`).
