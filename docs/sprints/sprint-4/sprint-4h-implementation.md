# SPOREKART v3.0 — SPRINT 4H IMPLEMENTATION DETAILS

---

### 1. Architectural Overview

Sprint 4H implements a production-grade **Post-Purchase Support, Disputes, Replacements & Customer Service Operations domain** (`com.sporekart.modules.support`):
- **Support Ticket Aggregate (`SupportTicket`)**: Owns ticket lifecycle (`OPEN`, `ASSIGNED`, `IN_PROGRESS`, `WAITING_FOR_CUSTOMER`, `WAITING_FOR_INTERNAL`, `ESCALATED`, `RESOLVED`, `CLOSED`, `REOPENED`, `CANCELLED`), category taxonomy (`ORDER`, `PAYMENT`, `DELIVERY`, `RETURN`, `REFUND`, `PRODUCT`, `REPLACEMENT`, `ACCOUNT`, `OTHER`), issue types, priority, agent assignment, and SLA tracking (`MET`, `AT_RISK`, `BREACHED`).
- **Conversation Engine (`SupportMessage`)**: Supports public messages (`CUSTOMER_VISIBLE`) and internal operational notes (`INTERNAL_NOTE`). Customer-facing API endpoints strictly strip internal notes.
- **Replacement Request Aggregate (`ReplacementRequest`)**: Manages replacement workflows (`RPL-2026-XXXXXX`), validates stock availability via `InventoryApplicationService.getInventoryBySku`, reserves stock via `InventoryApplicationService.reserveInventoryForOrder`, and requests replacement shipment booking via `ShipmentApplicationService.createShipmentForOrder`.
- **Flyway Database Migration (`V14`)**: Creates `support_tickets`, `support_messages`, `replacement_requests`, and `support_ticket_status_history` with database indexes and uniqueness constraints (`ticket_number`, `replacement_reference`).
- **REST Endpoints**: Customer endpoints (`/api/v1/customer/support/tickets/**`) and Admin support endpoints (`/api/v1/admin/support/tickets/**`, `/api/v1/admin/support/replacements/**`).
- **Frontend Service (`supportApi.ts`)**: Axios service wrapping ticket creation, customer conversations, reopen, replacement requests, and admin support dashboard actions.

---

### 2. File Changes Summary

#### Backend (`com.sporekart.modules.support`)
- Domain Enums: `TicketCategory`, `IssueType`, `TicketStatus`, `TicketPriority`, `TicketSource`, `MessageVisibility`, `AuthorType`, `SlaStatus`, `ReplacementStatus`.
- Entities: `SupportTicket.java`, `SupportMessage.java`, `ReplacementRequest.java`, `SupportTicketStatusHistory.java`.
- Domain Services & Events: `SupportStateMachine.java`, `SlaCalculationService.java`, `SupportTicketCreatedEvent`, `SupportTicketAssignedEvent`, `SupportTicketRepliedEvent`, `SupportTicketEscalatedEvent`, `SupportTicketResolvedEvent`, `SupportTicketClosedEvent`, `ReplacementRequestedEvent`, `ReplacementApprovedEvent`.
- Repositories: `SupportTicketRepository`, `SupportMessageRepository`, `ReplacementRequestRepository`, `SupportTicketStatusHistoryRepository`.
- Application Layer: `SupportApplicationService.java`, `CreateTicketRequestDto`, `AddMessageRequestDto`, `AssignTicketRequestDto`, `CreateReplacementRequestDto`, `SupportTicketDto`, `SupportMessageDto`, `ReplacementRequestDto`, `SupportOrderContextDto`, `AdminTicketFilterDto`.
- Controllers: `CustomerSupportController.java`, `AdminSupportController.java`.

#### Frontend (`frontend/src/`)
- `endpoints.ts`: Added customer and admin support endpoints.
- `supportApi.ts`: Axios API service.

#### Database Migration
- `V14__support_disputes_replacements_domain.sql`: Schema definition.
