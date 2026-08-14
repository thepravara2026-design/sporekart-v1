# Sprint 0 Deferred Work Register — Sporekart v3.0

The following business features are explicitly deferred from Sprint 0 to maintain strict architectural foundation boundaries.

---

ID: AUTH-001
DESCRIPTION: Customer authentication (registration, login, JWT issuance, password reset)
REASON: Sprint 0 is strictly limited to engineering foundation and baseline health APIs.
OWNER SPRINT: Sprint 1 — Authentication & Identity
DEPENDENCIES: Sprint 0 Engineering Foundation
PRIORITY: HIGH

---

ID: USER-001
DESCRIPTION: Customer profile management, roles (CUSTOMER, STAFF, ADMIN), address book
REASON: User profile management is owned by Sprint 1.
OWNER SPRINT: Sprint 1 — Authentication & Identity
DEPENDENCIES: AUTH-001
PRIORITY: HIGH

---

ID: CAT-001
DESCRIPTION: Product catalog CRUD, categories, product listing, search, filtering, product details
REASON: Catalog business logic is owned by Sprint 2.
OWNER SPRINT: Sprint 2 — Catalog & Product Management
DEPENDENCIES: Sprint 0 Foundation
PRIORITY: HIGH

---

ID: INV-001
DESCRIPTION: Inventory stock management, warehouse stock reservations
REASON: Inventory functionality is owned by Sprint 3.
OWNER SPRINT: Sprint 3 — Inventory
DEPENDENCIES: CAT-001
PRIORITY: MEDIUM

---

ID: CART-001
DESCRIPTION: Shopping cart add/remove/update items, draft cart persistence
REASON: Cart functionality is owned by Sprint 4.
OWNER SPRINT: Sprint 4 — Cart
DEPENDENCIES: CAT-001
PRIORITY: HIGH

---

ID: ORD-001
DESCRIPTION: Order creation, order state machine, checkout processing
REASON: Order processing is owned by Sprint 5.
OWNER SPRINT: Sprint 5 — Checkout & Orders
DEPENDENCIES: CART-001, INV-001
PRIORITY: HIGH

---

ID: PAY-001
DESCRIPTION: Razorpay payment gateway integration, webhook signature verification
REASON: Payments functionality is owned by Sprint 6.
OWNER SPRINT: Sprint 6 — Payments
DEPENDENCIES: ORD-001
PRIORITY: HIGH

---

ID: SHIP-001
DESCRIPTION: Shiprocket integration, shipment tracking, delivery partner abstraction
REASON: Shipment abstraction is owned by Sprint 7.
OWNER SPRINT: Sprint 7 — Shipment Abstraction & Delivery Partners
DEPENDENCIES: ORD-001
PRIORITY: MEDIUM

---

ID: NOTIF-001
DESCRIPTION: Email and SMS notifications for order updates
REASON: Notifications functionality is owned by Sprint 8.
OWNER SPRINT: Sprint 8 — Notifications
DEPENDENCIES: ORD-001, SHIP-001
PRIORITY: LOW
