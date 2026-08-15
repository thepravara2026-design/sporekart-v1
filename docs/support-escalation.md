# SPOREKART v3.0 — SUPPORT ESCALATION SPECIFICATION

---

## 1. Escalation Triggers

1. **SLA Breach**: Automatic escalation when `firstResponseDueAt` or `resolutionDueAt` is breached.
2. **High Severity / Priority Upgrade**: Priority upgraded to `URGENT` by customer or agent.
3. **Manual Agent Escalation**: Support agent escalates complex dispute or cross-domain exception.

---

## 2. Escalation Workflow

```
   Normal Ticket
         ↓
  Escalation Event Trigger
         ↓
State -> ESCALATED
Priority -> URGENT / HIGH
         ↓
Reassign to Tier-2 / Lead Agent
         ↓
Notify Admin & Record Audit Event
```
