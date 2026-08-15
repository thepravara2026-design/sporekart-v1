# SPOREKART v3.0 — CUSTOMER SUPPORT & POST-PURCHASE OPERATIONS ARCHITECTURE

---

## 1. Subsystem Architecture & Orchestration Model

```
                             CUSTOMER
                                |
                                v
                          SUPPORT DOMAIN
                 (SupportTicket Aggregate Root)
                                |
      +-------------------------+-------------------------+
      |                         |                         |
      v                         v                         v
CONVERSATION & SLA      REPLACEMENT WORKFLOW       OPERATIONAL ACTIONS
(Public Messages &    (Inventory Check ->       (Order/Shipping/Return/
 Internal Notes)       Stock Reservation ->       Refund Cross-Domain
                       Replacement Shipment)        Read/Retry Hooks)
```

---

## 2. Core Domain Invariants

1. **Domain Boundary Isolation**: Support coordinates post-purchase issues, agent communication, and operational escalations. Authoritative domain state for Orders, Payments, Shipping, Returns, and Inventory remains in those respective modules.
2. **Internal Notes Privacy**: Support messages marked `INTERNAL_NOTE` are visible only to support agents/administrators and are strictly stripped from all customer-facing API responses.
3. **Idempotency & Concurrency**: Unique ticket numbers (`TKT-2026-XXXXXX`) and replacement references (`RPL-2026-XXXXXX`); `@Version` optimistic locking prevents concurrent state mutation conflicts.
4. **SLA Compliance Engine**: Server-side SLA evaluation (`MET`, `AT_RISK`, `BREACHED`) tracks first response and resolution compliance based on ticket priority.
