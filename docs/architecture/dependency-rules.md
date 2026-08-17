# Dependency Rules — Sporekart v3.0

## Layered Architecture Dependency Flow

Dependencies must strictly flow inward:

```
[ Controller ]
      │
      ▼
[ Application ]
      │
      ▼
[   Domain    ]
      ▲
      │
[ Infrastructure ]
```

### Flow Definitions

- **Controller Layer**: Handles HTTP requests/responses, request validation (`@Valid`), and mapping to DTOs.
- **Application Layer**: Orchestrates business workflows, transaction boundaries (`@Transactional`), and cross-domain events.
- **Domain Layer**: Holds core domain logic, business rules, entities, and value objects. Contains zero dependencies on web or framework infrastructure.
- **Infrastructure Layer**: Implements persistence adapters (JPA/Hibernate), external REST clients, and third-party integrations.

## Code Rules

- `com.sporekart.common` may be referenced by any domain module.
- Domain modules MUST NOT import internal classes from other domain modules (e.g. `com.sporekart.modules.order` cannot import `com.sporekart.modules.catalog.infrastructure.*`).
- Circular module dependencies are strictly prohibited and will fail static build checks.
