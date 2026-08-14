# ADR 0001: Catalog Module Architecture & DTO Contract Encapsulation

## Context
Sporekart v3.0 requires a scalable catalog browsing engine for mushroom cultures, supplies, and equipment. We needed to choose between a microservices deployment, a monolithic architecture, or a modular monolith.

## Decision
We decided to adopt a **Modular Monolith** architecture:
1. **Backend**: Single Spring Boot application with strictly bounded Java packages (`com.sporekart.modules.catalog`).
2. **Persistence**: JPA / Hibernate entities (`ProductEntity`, `CategoryEntity`) encapsulated within the infrastructure layer.
3. **API Contracts**: Controllers return strongly typed DTOs (`ProductDto`, `CategoryDto`, `ApiResponse<T>`, `PageResponse<T>`). JPA Entities are forbidden from escaping controller boundaries.
4. **Testing Database**: H2 in-memory DB in PostgreSQL compatibility mode for rapid, deterministic local DEV/QAT integration testing.

## Consequences
- **Positive**: Zero network overhead between internal domain modules; rapid build and test cycles; clear domain boundary isolation.
- **Negative**: Requires strict discipline to prevent cross-package database joins.
