# SPOREKART v3.0 — CATALOG TESTING STRATEGY & SUITE

## 1. Test Pyramid

```
        / \
       /   \     E2E Customer Journey Tests (Vitest + React Router MemoryRouter)
      / ---- \    API Integration & Contract Tests (CatalogApiContractTest)
     / ------ \   Backend Integration & Repository Tests (CatalogPersistenceTest)
    / ---------- \ Unit Tests (Domain, Application Services, Components)
```

---

## 2. Test Execution Commands

### 2.1 Backend Tests (Maven + JUnit 5 + Spring Boot Test)
```bash
cd backend
mvn clean test
```
*Executes all 50 backend unit, domain, controller, application service, and API contract tests against the H2 in-memory test database.*

### 2.2 Frontend Unit & Integration Tests (Vitest + Testing Library)
```bash
cd frontend
npm run test
```
*Executes all 20 frontend component, hook, API client, and E2E customer journey tests.*

### 2.3 Frontend Lint & Type Checks
```bash
cd frontend
npm run lint
npm run build
```
*Enforces ESLint strict zero-warning policy and TypeScript compilation.*

---

## 3. Test Fixtures & H2 In-Memory Database

### 3.1 Backend In-Memory Database Profile (`test` / `qat`)
- Database: H2 in-memory database (`jdbc:h2:mem:sporekartdb`)
- Mode: PostgreSQL compatibility mode (`MODE=PostgreSQL`)
- Data Isolation: Schema recreated and Flyway migrations applied per test run.
- Seeding: `CatalogDataSeeder.java` populates deterministic DEV/QAT products and categories when active profile is `dev` or `qat`.

### 3.2 E2E Customer Journey Suite (`CatalogEndToEnd.test.tsx`)
Verifies the 10-point customer release journey:
1. Product listing rendering
2. Keyword search
3. Category filtering
4. Price bounds filtering (`minPrice`, `maxPrice`)
5. Sorting
6. Pagination
7. Category navigation
8. Product Detail viewing
9. 404 Product Not Found handling
10. Network failure & retry recovery
