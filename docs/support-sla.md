# SPOREKART v3.0 — SUPPORT SLA SPECIFICATION

---

## 1. SLA Matrix by Ticket Priority

| Priority | First Response SLA Window | Resolution SLA Window | At-Risk Threshold |
| :--- | :--- | :--- | :--- |
| **URGENT** | 4 hours | 12 hours | 75% of window elapsed |
| **HIGH** | 12 hours | 24 hours | 75% of window elapsed |
| **NORMAL** | 24 hours | 48 hours | 75% of window elapsed |
| **LOW** | 48 hours | 72 hours | 75% of window elapsed |

---

## 2. SLA Compliance Logic

- **First Response SLA**: Evaluated when an `AGENT` sends a `CUSTOMER_VISIBLE` reply. Internal notes do not count as a first response.
- **Resolution SLA**: Evaluated when ticket transitions to `RESOLVED` status.
- **SLA Status States**:
  - `MET`: Completed within target window.
  - `AT_RISK`: Current elapsed time > 75% of target window without completion.
  - `BREACHED`: Current elapsed time > 100% of target window without completion.
