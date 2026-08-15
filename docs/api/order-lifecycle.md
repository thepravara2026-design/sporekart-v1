# SPOREKART v3.0 — Order Lifecycle API Reference

## API Specification

---

### 1. Generic State Transition Endpoint (Admin)
- **POST** `/api/v1/admin/orders/{orderId}/transitions`
- **Security**: Requires `ROLE_ADMIN`

#### Request Body
```json
{
  "targetStatus": "CONFIRMED",
  "reason": "Administrative manual confirmation"
}
```

#### Success Response (`200 OK`)
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "orderNumber": "ORD-20260815-100001",
    "status": "CONFIRMED",
    "grandTotal": 1499.00,
    "currency": "INR",
    "createdAt": "2026-08-15T10:00:00Z",
    "updatedAt": "2026-08-15T10:05:00Z"
  },
  "message": "Order status updated successfully",
  "timestamp": "2026-08-15T10:05:00Z"
}
```

#### Invalid Transition Response (`400 Bad Request`)
```json
{
  "success": false,
  "errorCode": "INVALID_ORDER_STATE_TRANSITION",
  "message": "Cannot transition order from CREATED to DELIVERED",
  "timestamp": "2026-08-15T10:05:00Z"
}
```

---

### 2. Operational Transitions
- `POST /api/v1/admin/orders/{orderId}/process`: `CONFIRMED` -> `PROCESSING`
- `POST /api/v1/admin/orders/{orderId}/ready-for-fulfilment`: `PROCESSING` -> `READY_FOR_FULFILMENT`
- `POST /api/v1/admin/orders/{orderId}/shipped`: `READY_FOR_FULFILMENT` -> `SHIPPED`
- `POST /api/v1/admin/orders/{orderId}/out-for-delivery`: `SHIPPED` -> `OUT_FOR_DELIVERY`
- `POST /api/v1/admin/orders/{orderId}/delivered`: `OUT_FOR_DELIVERY` -> `DELIVERED`
- `POST /api/v1/admin/orders/{orderId}/complete`: `DELIVERED` -> `COMPLETED`
- `POST /api/v1/admin/orders/{orderId}/cancel`: Administrative cancellation
