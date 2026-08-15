# SPOREKART v3.0 — SUPPORT TICKET LIFECYCLE & STATE MACHINE SPECIFICATION

---

## 1. Ticket Lifecycle State Diagram

```
         OPEN
          ↓
       ASSIGNED  ----------> CANCELLED (Terminal)
          ↓
     IN_PROGRESS <-------> WAITING_FOR_CUSTOMER
          ↓
 WAIT_FOR_INTERNAL
          ↓
      ESCALATED
          ↓
      RESOLVED  ----------> REOPENED (Transition to IN_PROGRESS)
          ↓
       CLOSED (Terminal)
```

---

## 2. Valid Transitions & Safeguards

- `OPEN` -> `ASSIGNED`, `IN_PROGRESS`, `CANCELLED`
- `ASSIGNED` -> `IN_PROGRESS`, `WAITING_FOR_CUSTOMER`, `ESCALATED`
- `IN_PROGRESS` -> `WAITING_FOR_CUSTOMER`, `WAITING_FOR_INTERNAL`, `ESCALATED`, `RESOLVED`
- `WAITING_FOR_CUSTOMER` -> `IN_PROGRESS`, `RESOLVED`
- `WAITING_FOR_INTERNAL` -> `IN_PROGRESS`, `RESOLVED`
- `ESCALATED` -> `IN_PROGRESS`, `RESOLVED`
- `RESOLVED` -> `CLOSED`, `REOPENED`
- `REOPENED` -> `IN_PROGRESS`, `ESCALATED`
- `CLOSED` / `CANCELLED`: Terminal states.
