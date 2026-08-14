# SPOREKART v3.0 — SPRINT 1C DEFERRED WORK REGISTER

The following items were identified during Sprint 1C execution as intentionally out-of-scope for Sprint 1C and have been explicitly deferred to their respective owner sprints.

---

### DEFERRED ITEMS

ID: CAT-SEARCH-001
DESCRIPTION: Advanced Full-Text Search / Elasticsearch / Fuzzy Matching / Relevance Ranking
WHY DEFERRED: Sprint 1C only provides basic SQL substring searching and pagination. Full-text search engine integration belongs to future search enhancements.
OWNER SPRINT: Future Search Sprint
DEPENDENCY: Search Engine Architecture
RISK: None for Sprint 1C. Basic filtering is fully functional.
RECOMMENDED ACTION: Preserve SQL substring filter until Elasticsearch/Search module is introduced.

---

ID: AUTH-001
DESCRIPTION: Customer Authentication, Login, Register, JWT, Refresh Token & Password Management
WHY DEFERRED: Sprint 1C focuses exclusively on Catalog API exposure. Authentication features belong strictly to the Authentication Sprint.
OWNER SPRINT: Authentication Sprint
DEPENDENCY: User Domain & Auth Service
RISK: None. Catalog GET endpoints are publicly accessible for browsing.
RECOMMENDED ACTION: Implement in Auth Sprint.

---

ID: UI-001
DESCRIPTION: Frontend Catalog UI, Product Listing Page, Product Detail Page, Category UI, Filter Component
WHY DEFERRED: Master prompt Section 4 explicitly forbids creating Catalog UI components in Sprint 1C.
OWNER SPRINT: Sprint 1D — Frontend Catalog Integration
DEPENDENCY: Sprint 1C Catalog API contract (docs/api/catalog-api.md)
RISK: None. API contract is complete and verified.
RECOMMENDED ACTION: Consume `/api/v1/catalog` endpoints in Sprint 1D.

---

ID: PAY-001
DESCRIPTION: Razorpay Payment Gateway Integration
WHY DEFERRED: Payment gateway integration is strictly out of scope for Sprint 1C.
OWNER SPRINT: Payment Sprint (Sprint 6)
DEPENDENCY: Order & Payment Domain
RISK: None.
RECOMMENDED ACTION: Implement in Sprint 6 using Mock Payment Provider for DEV/QA.

---

ID: SHIP-001
DESCRIPTION: Shiprocket Shipment Integration
WHY DEFERRED: Shipping provider integration is strictly out of scope for Sprint 1C.
OWNER SPRINT: Shipment Sprint (Sprint 7)
DEPENDENCY: Shipment Domain
RISK: None.
RECOMMENDED ACTION: Implement in Sprint 7.
