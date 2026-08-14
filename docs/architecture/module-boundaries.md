# Module Boundaries — Sporekart v3.0

## Module Map

Sporekart v3.0 backend is organized into conceptual domain modules located under `com.sporekart`:

```
com.sporekart
├── common        # Shared kernel (DTOs, exception handlers, security baseline)
├── auth          # Authentication & Token Management (Sprint 1)
├── user          # User Profiles & Customer Data (Sprint 1)
├── catalog       # Product & Category Management (Sprint 2)
├── inventory     # Stock & Warehouse Management (Sprint 3)
├── cart          # Shopping Cart & Draft Items (Sprint 4)
├── order         # Order Processing & Management (Sprint 5)
├── payment       # Payment Gateways & Razorpay (Sprint 6)
├── shipment      # Shipment Abstraction & Shiprocket (Sprint 7)
├── notification  # Email & SMS Notifications (Sprint 8)
└── admin         # Operations Dashboard Services (Sprint 9)
```

## Standard Internal Module Structure

Each module follows a strict layered architecture:

```
com.sporekart.<module>
├── controller/     # REST Endpoints (/api/v1/...)
├── application/    # Application Services & Use-Case Orchestration
├── domain/         # Entities, Value Objects, Domain Exceptions
└── infrastructure/ # JPA Repositories & External API Adapters
```

## Communication Rules

1. **Controllers** delegate directly to Application Services in the same module. Controllers MUST NOT access JPA Repositories directly.
2. **Cross-Module Calls**: A service in `Order` module needing `Product` information must invoke `CatalogService` interface, NOT `ProductRepository` directly.
3. **No Circular Dependencies**: Module dependencies must form a Directed Acyclic Graph (DAG).
