# SPOREKART v3.0 — SPRINT 1E COMPREHENSIVE TEST MATRIX

This matrix records test scenarios across backend, frontend, integration, security, and browser validation for Sprint 1.

---

## TEST MATRIX

| Test ID | Scenario Description | Layer | Execution Command / Class | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|---|
| TEST-BK-001 | Product domain entity invariants (SKU, price, status) | Backend Domain | `ProductDomainTest` | Invariants enforced | Pass | PASS |
| TEST-BK-002 | Category domain entity invariants (name, slug) | Backend Domain | `CategoryDomainTest` | Slug format enforced | Pass | PASS |
| TEST-BK-003 | Spring Data JPA persistence and H2 mapping | Backend Repository | `CatalogPersistenceTest` | Entities persisted & queried | Pass | PASS |
| TEST-BK-004 | Product application query service pagination & sort validation | Backend Service | `ProductApplicationServiceTest` | Whitelist enforced, Page returned | Pass | PASS |
| TEST-BK-005 | Category application query service pagination & sort validation | Backend Service | `CategoryApplicationServiceTest` | Whitelist enforced, Page returned | Pass | PASS |
| TEST-BK-006 | Product REST controller MockMvc response validation | Backend Controller | `CatalogProductControllerTest` | 200 OK with DTO payload | Pass | PASS |
| TEST-BK-007 | Category REST controller MockMvc response validation | Backend Controller | `CatalogCategoryControllerTest` | 200 OK with DTO payload | Pass | PASS |
| TEST-BK-008 | Catalog API full integration test against H2 | Backend Integration | `CatalogApiIntegrationTest` | E2E HTTP -> Controller -> DB | Pass | PASS |
| TEST-BK-009 | DEV profile H2 startup & seed runner execution | Backend Profile | `DevProfileTest` | H2 started, seed data loaded | Pass | PASS |
| TEST-BK-010 | QAT profile H2 startup & seed runner execution | Backend Profile | `QatProfileTest` | H2 started, seed data loaded | Pass | PASS |
| TEST-FE-001 | `catalogApi` endpoint request construction & params mapping | Frontend API Client | `catalogApi.test.ts` | Axios called with valid URLs/params | Pass | PASS |
| TEST-FE-002 | `ProductCard` component rendering & price formatting | Frontend Component | `ProductCard.test.tsx` | SKU, price, status, link rendered | Pass | PASS |
| TEST-FE-003 | `ProductListPage` happy path product grid rendering | Frontend Page | `ProductListPage.test.tsx` | Products loaded from API | Pass | PASS |
| TEST-FE-004 | `ProductListPage` empty result state handling | Frontend Page | `ProductListPage.test.tsx` | Empty state & clear button shown | Pass | PASS |
| TEST-FE-005 | `ProductDetailPage` happy path product view | Frontend Page | `ProductDetailPage.test.tsx` | Product details rendered | Pass | PASS |
| TEST-FE-006 | `ProductDetailPage` 404 Product Not Found state | Frontend Page | `ProductDetailPage.test.tsx` | Dedicated 404 alert displayed | Pass | PASS |
| TEST-FE-007 | Frontend ESLint zero warning enforcement | Frontend Quality | `npm run lint` | 0 warnings, 0 errors | Pass | PASS |
| TEST-FE-008 | Frontend production TypeScript build | Frontend Build | `npm run build` | Bundle generated (`dist/`) | Pass | PASS |
| TEST-SEC-001 | Public permitAll GET /api/v1/catalog/** security check | Security | `SecurityConfigTest` | GET permitted without auth | Pass | PASS |
| TEST-SEC-002 | Secret scanning check | Security | Git inspection | Zero secrets committed | Pass | PASS |
| TEST-ACC-001 | Accessible HTML5 headings, ARIA labels, focus rings | Accessibility | Keyboard & Screen Reader | Screen reader & keyboard accessible | Pass | PASS |
