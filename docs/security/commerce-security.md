# SPOREKART v3.0 — Commerce Security Baseline & Hardening Document

## Security Principles & Control Matrix

---

### 1. Insecure Direct Object Reference (IDOR) Protection
- All application services (`OrderApplicationService`, `CartApplicationService`, `ReturnApplicationService`, `ShipmentApplicationService`) explicitly validate that the requesting `customerId` matches the owner of the resource.
- Access attempts by unauthorized customer accounts result in immediate access rejection without revealing entity existence details.

### 2. Financial & Authoritative Price Computation
- Frontend clients CANNOT supply prices, subtotals, or item totals to checkout APIs.
- The `CheckoutApplicationService` re-computes line totals dynamically by querying the authoritative `CatalogModule` product repository.

### 3. Payment Credential Safety & Webhooks
- SPOREKART NEVER logs or stores raw credit card numbers, CVVs, or gateway secret keys.
- Webhook endpoints validate incoming payload signatures (`HMAC-SHA256`) before processing callbacks.

### 4. Database & Input Validation
- Bean Validation (`@NotNull`, `@Min(1)`, `@Size`) enforces input limits at controller entry points.
- Database CHECK constraints enforce non-negative quantities and amounts at the persistence layer.
