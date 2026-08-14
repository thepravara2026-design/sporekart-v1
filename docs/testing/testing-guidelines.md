# Testing Architecture & Guidelines — Sporekart v3.0

## Testing Pyramid

```
             E2E / Smoke Tests
                  ^
                  |
          Integration Tests
                  ^
                  |
             Unit Tests
```

## Backend Testing
- **Framework**: JUnit 5, Mockito, Spring Boot Test, Testcontainers.
- **Unit Tests**: Test domain logic, controllers, and exception mapping in isolation.
- **Integration Tests**: `@SpringBootTest` with Spring Security MockMvc testing.

## Frontend Testing
- **Framework**: Vitest, React Testing Library, jsdom.
- **Component Tests**: Test React component rendering, state transitions, and user events.
- **Service Tests**: Mock Axios calls using Vitest spies.
