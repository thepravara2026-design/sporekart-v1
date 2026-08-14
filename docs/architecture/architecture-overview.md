# Architecture Overview — Sporekart v3.0

## System Vision

Sporekart v3.0 is designed as a single-deployable **Modular Monolith** powering an enterprise e-commerce platform. It balances long-term maintainability, clean domain boundaries, and simple operational deployment without the premature complexity of microservices.

## Conceptual Diagram

```
                    SPOREKART

                         |
                         v

              +---------------------+
              |      FRONTEND       |
              |    Vite + React     |
              |     TypeScript      |
              +----------+----------+
                         |
                       REST
                         |
                         v
              +---------------------+
              |    SPRING BOOT      |
              |    API /api/v1      |
              +----------+----------+
                         |
        +----------------+----------------+
        |                |                |
        v                v                v
      AUTH            CATALOG          INVENTORY
        |                |                |
        +----------------+----------------+
        |                |                |
        v                v                v
       CART            ORDER           PAYMENT
                         |
                         v
                     SHIPMENT
                         |
                         v
                   NOTIFICATION
                         |
                         v
                  POSTGRESQL
```

## Architectural Principles

1. **Single Backend Deployment**: All domain modules reside within the single Spring Boot application artifact (`sporekart-backend.jar`).
2. **Domain Isolation**: Each module (`auth`, `catalog`, `inventory`, `cart`, `order`, `payment`, `shipment`, `notification`, `admin`, `user`) defines clear internal package boundaries.
3. **Decoupled Data Access**: Direct cross-module database table joins or repository access are strictly forbidden. Communication across domain boundaries occurs exclusively through explicit public domain interfaces or application services.
4. **Contract-First REST API**: All external communication between frontend and backend is over HTTP REST endpoints prefixed with `/api/v1`.
5. **Configuration-Driven Environments**: All environment specifics (database connection parameters, CORS origins, secrets) are managed via standard environment variables.
