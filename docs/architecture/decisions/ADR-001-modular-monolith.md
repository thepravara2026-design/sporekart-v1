# ADR-001: Modular Monolith Architecture

## Status
Accepted

## Context
Sporekart v3.0 requires a robust, scalable backend architecture for e-commerce processing. We evaluated whether to build a distributed microservice architecture vs. a unified modular monolith.

## Decision
We choose a **Modular Monolith** architecture packaged in a single Spring Boot executable artifact (`sporekart-backend.jar`).

## Alternatives Considered
- **Microservices Architecture**: Rejected due to network latency overhead, distributed transaction management complexity (Sagas), deployment overhead, and premature operational complexity at current scale.
- **Traditional Monolith (Unstructured)**: Rejected due to risk of spaghetti code, tight coupling, and lack of clear boundary enforcement.

## Consequences
- **Positive**: Simplified deployment, single transaction context, zero inter-service network overhead, straightforward testing and local development.
- **Negative**: Requires strict discipline and static code enforcement to prevent domain boundary bleeding.
