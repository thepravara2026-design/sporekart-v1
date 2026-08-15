# SPOREKART v3.0 — REVIEW MODERATION LIFECYCLE SPECIFICATION

---

## 1. Review Moderation Lifecycle State Diagram

```
         PENDING_MODERATION
                 ↓
      +----------┴----------+
      |                     |
   APPROVED              REJECTED (Terminal)
      |                     ^
      v                     |
   FLAGGED -----------------+
```

---

## 2. Transition Safeguards

- `PENDING_MODERATION` -> `APPROVED` (Auto-pass via spam filter or admin approval)
- `PENDING_MODERATION` -> `REJECTED` (Rejection by admin or automated spam block)
- `APPROVED` -> `FLAGGED` (Flagged by customer or system audit)
- `FLAGGED` -> `APPROVED` (Re-cleared by admin)
- `FLAGGED` -> `REJECTED` (Rejected upon admin review)
- `REJECTED`: Terminal state.
