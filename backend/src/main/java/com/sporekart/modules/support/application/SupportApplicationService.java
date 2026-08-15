package com.sporekart.modules.support.application;

import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.inventory.application.dto.InventoryItemDto;
import com.sporekart.modules.shipment.application.ShipmentApplicationService;
import com.sporekart.modules.support.application.dto.*;
import com.sporekart.modules.support.domain.*;
import com.sporekart.modules.support.domain.event.*;
import com.sporekart.modules.support.domain.exception.ReplacementNotFoundException;
import com.sporekart.modules.support.domain.exception.SupportAccessDeniedException;
import com.sporekart.modules.support.domain.exception.TicketNotFoundException;
import com.sporekart.modules.support.infrastructure.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SupportApplicationService {

    private static final Logger log = LoggerFactory.getLogger(SupportApplicationService.class);

    private final SupportTicketRepository ticketRepository;
    private final SupportMessageRepository messageRepository;
    private final ReplacementRequestRepository replacementRepository;
    private final SupportTicketStatusHistoryRepository historyRepository;
    private final SlaCalculationService slaCalculationService;
    private final InventoryApplicationService inventoryApplicationService;
    private final ShipmentApplicationService shipmentApplicationService;
    private final ApplicationEventPublisher eventPublisher;

    public SupportApplicationService(
            SupportTicketRepository ticketRepository,
            SupportMessageRepository messageRepository,
            ReplacementRequestRepository replacementRepository,
            SupportTicketStatusHistoryRepository historyRepository,
            SlaCalculationService slaCalculationService,
            InventoryApplicationService inventoryApplicationService,
            ShipmentApplicationService shipmentApplicationService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.ticketRepository = ticketRepository;
        this.messageRepository = messageRepository;
        this.replacementRepository = replacementRepository;
        this.historyRepository = historyRepository;
        this.slaCalculationService = slaCalculationService;
        this.inventoryApplicationService = inventoryApplicationService;
        this.shipmentApplicationService = shipmentApplicationService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public SupportTicketDto createCustomerTicket(CreateTicketRequestDto dto, String customerId) {
        log.info("Creating support ticket for customer: {}, subject: {}", customerId, dto.subject());

        String currentYear = String.valueOf(LocalDate.now().getYear());
        long count = ticketRepository.countTicketsForYear(currentYear);
        String ticketNumber = String.format("TKT-%s-%06d", currentYear, count + 1);

        SupportTicket ticket = SupportTicket.createNew(
                ticketNumber,
                customerId,
                dto.orderId(),
                dto.orderReference(),
                dto.category(),
                dto.issueType(),
                dto.priority(),
                dto.source(),
                dto.subject(),
                dto.description()
        );

        SupportTicket savedTicket = ticketRepository.save(ticket);

        // Save initial customer description as first message
        SupportMessage initialMessage = SupportMessage.create(
                savedTicket.getId(),
                customerId,
                AuthorType.CUSTOMER,
                MessageVisibility.CUSTOMER_VISIBLE,
                dto.description(),
                dto.attachmentUrls()
        );
        messageRepository.save(initialMessage);

        historyRepository.save(SupportTicketStatusHistory.record(
                savedTicket.getId(),
                null,
                savedTicket.getStatus().name(),
                "Ticket created by customer",
                "CUSTOMER",
                customerId
        ));

        eventPublisher.publishEvent(SupportTicketCreatedEvent.create(
                savedTicket.getId(),
                savedTicket.getTicketNumber(),
                savedTicket.getCustomerId(),
                savedTicket.getOrderId(),
                savedTicket.getCategory(),
                savedTicket.getPriority()
        ));

        return getTicketDtoWithFilter(savedTicket, true);
    }

    @Transactional
    public SupportTicketDto addCustomerMessage(String ticketNumber, AddMessageRequestDto dto, String customerId) {
        SupportTicket ticket = getTicketByNumberOrId(ticketNumber);
        validateCustomerOwnership(ticket, customerId);

        if (ticket.getStatus().isTerminal()) {
            throw new IllegalStateException("Cannot reply to a closed or cancelled support ticket: " + ticketNumber);
        }

        SupportMessage message = SupportMessage.create(
                ticket.getId(),
                customerId,
                AuthorType.CUSTOMER,
                MessageVisibility.CUSTOMER_VISIBLE,
                dto.content(),
                dto.attachmentUrls()
        );
        messageRepository.save(message);

        if (ticket.getStatus() == TicketStatus.WAITING_FOR_CUSTOMER) {
            TicketStatus prev = ticket.getStatus();
            ticket.updateStatus(TicketStatus.IN_PROGRESS);
            ticketRepository.save(ticket);
            historyRepository.save(SupportTicketStatusHistory.record(
                    ticket.getId(), prev.name(), ticket.getStatus().name(), "Customer replied", "CUSTOMER", customerId
            ));
        }

        eventPublisher.publishEvent(SupportTicketRepliedEvent.create(
                ticket.getId(), ticket.getTicketNumber(), customerId, AuthorType.CUSTOMER, MessageVisibility.CUSTOMER_VISIBLE
        ));

        return getTicketDtoWithFilter(ticket, true);
    }

    @Transactional
    public SupportTicketDto addAgentMessage(String ticketNumber, AddMessageRequestDto dto, String adminId) {
        SupportTicket ticket = getTicketByNumberOrId(ticketNumber);

        MessageVisibility visibility = dto.visibility() != null ? dto.visibility() : MessageVisibility.CUSTOMER_VISIBLE;

        SupportMessage message = SupportMessage.create(
                ticket.getId(),
                adminId,
                AuthorType.AGENT,
                visibility,
                dto.content(),
                dto.attachmentUrls()
        );
        messageRepository.save(message);

        if (visibility == MessageVisibility.CUSTOMER_VISIBLE) {
            ticket.recordFirstResponse();
            if (ticket.getStatus() == TicketStatus.OPEN || ticket.getStatus() == TicketStatus.ASSIGNED) {
                TicketStatus prev = ticket.getStatus();
                ticket.updateStatus(TicketStatus.IN_PROGRESS);
                historyRepository.save(SupportTicketStatusHistory.record(
                        ticket.getId(), prev.name(), ticket.getStatus().name(), "Agent replied to customer", "AGENT", adminId
                ));
            }
            ticketRepository.save(ticket);
        }

        eventPublisher.publishEvent(SupportTicketRepliedEvent.create(
                ticket.getId(), ticket.getTicketNumber(), adminId, AuthorType.AGENT, visibility
        ));

        return getTicketDtoWithFilter(ticket, false);
    }

    @Transactional
    public SupportTicketDto assignTicket(String ticketNumber, String agentId, String adminId) {
        SupportTicket ticket = getTicketByNumberOrId(ticketNumber);
        TicketStatus prev = ticket.getStatus();
        ticket.assign(agentId);
        ticketRepository.save(ticket);

        historyRepository.save(SupportTicketStatusHistory.record(
                ticket.getId(), prev.name(), ticket.getStatus().name(), "Assigned to " + agentId, "ADMIN", adminId
        ));

        eventPublisher.publishEvent(SupportTicketAssignedEvent.create(ticket.getId(), ticket.getTicketNumber(), agentId));
        return getTicketDtoWithFilter(ticket, false);
    }

    @Transactional
    public SupportTicketDto updatePriority(String ticketNumber, TicketPriority priority, String adminId) {
        SupportTicket ticket = getTicketByNumberOrId(ticketNumber);
        ticket.updatePriority(priority);
        ticketRepository.save(ticket);
        return getTicketDtoWithFilter(ticket, false);
    }

    @Transactional
    public SupportTicketDto escalateTicket(String ticketNumber, String reason, String adminId) {
        SupportTicket ticket = getTicketByNumberOrId(ticketNumber);
        TicketStatus prev = ticket.getStatus();
        ticket.escalate(reason);
        ticketRepository.save(ticket);

        historyRepository.save(SupportTicketStatusHistory.record(
                ticket.getId(), prev.name(), ticket.getStatus().name(), "Escalated: " + reason, "ADMIN", adminId
        ));

        eventPublisher.publishEvent(SupportTicketEscalatedEvent.create(ticket.getId(), ticket.getTicketNumber(), reason));
        return getTicketDtoWithFilter(ticket, false);
    }

    @Transactional
    public SupportTicketDto resolveTicket(String ticketNumber, String resolutionNotes, String adminId) {
        SupportTicket ticket = getTicketByNumberOrId(ticketNumber);
        TicketStatus prev = ticket.getStatus();
        ticket.resolve(resolutionNotes);
        ticketRepository.save(ticket);

        if (resolutionNotes != null && !resolutionNotes.isBlank()) {
            SupportMessage msg = SupportMessage.create(
                    ticket.getId(), adminId, AuthorType.AGENT, MessageVisibility.CUSTOMER_VISIBLE,
                    "Ticket Resolved: " + resolutionNotes, null
            );
            messageRepository.save(msg);
        }

        historyRepository.save(SupportTicketStatusHistory.record(
                ticket.getId(), prev.name(), ticket.getStatus().name(), "Resolved by admin", "ADMIN", adminId
        ));

        eventPublisher.publishEvent(SupportTicketResolvedEvent.create(ticket.getId(), ticket.getTicketNumber(), ticket.getCustomerId()));
        return getTicketDtoWithFilter(ticket, false);
    }

    @Transactional
    public SupportTicketDto reopenTicket(String ticketNumber, String reason, String customerId) {
        SupportTicket ticket = getTicketByNumberOrId(ticketNumber);
        validateCustomerOwnership(ticket, customerId);

        TicketStatus prev = ticket.getStatus();
        ticket.reopen(reason);
        ticketRepository.save(ticket);

        SupportMessage msg = SupportMessage.create(
                ticket.getId(), customerId, AuthorType.CUSTOMER, MessageVisibility.CUSTOMER_VISIBLE,
                "Ticket Reopened: " + reason, null
        );
        messageRepository.save(msg);

        historyRepository.save(SupportTicketStatusHistory.record(
                ticket.getId(), prev.name(), ticket.getStatus().name(), "Reopened by customer: " + reason, "CUSTOMER", customerId
        ));

        return getTicketDtoWithFilter(ticket, true);
    }

    @Transactional
    public SupportTicketDto closeTicket(String ticketNumber, String adminId) {
        SupportTicket ticket = getTicketByNumberOrId(ticketNumber);
        TicketStatus prev = ticket.getStatus();
        ticket.close();
        ticketRepository.save(ticket);

        historyRepository.save(SupportTicketStatusHistory.record(
                ticket.getId(), prev.name(), ticket.getStatus().name(), "Closed by admin", "ADMIN", adminId
        ));

        eventPublisher.publishEvent(SupportTicketClosedEvent.create(ticket.getId(), ticket.getTicketNumber(), ticket.getCustomerId()));
        return getTicketDtoWithFilter(ticket, false);
    }

    @Transactional
    public ReplacementRequestDto requestReplacement(String ticketNumber, CreateReplacementRequestDto dto, String customerId) {
        SupportTicket ticket = getTicketByNumberOrId(ticketNumber);
        validateCustomerOwnership(ticket, customerId);

        // Validate SKU availability
        InventoryItemDto invDto = inventoryApplicationService.getInventoryBySku(dto.sku());
        if (invDto.availableQuantity() < dto.quantity()) {
            log.warn("Replacement requested for SKU {} with insufficient available stock ({})", dto.sku(), invDto.availableQuantity());
        }

        String currentYear = String.valueOf(LocalDate.now().getYear());
        long count = replacementRepository.countReplacementsForYear(currentYear);
        String replacementRef = String.format("RPL-%s-%06d", currentYear, count + 1);

        ReplacementRequest replacement = ReplacementRequest.create(
                replacementRef,
                ticket.getId(),
                ticket.getTicketNumber(),
                dto.orderId(),
                dto.orderReference(),
                customerId,
                dto.orderItemId(),
                dto.productId(),
                dto.sku(),
                dto.quantity(),
                dto.reason()
        );

        ReplacementRequest saved = replacementRepository.save(replacement);

        eventPublisher.publishEvent(ReplacementRequestedEvent.create(
                saved.getId(), saved.getReplacementReference(), ticket.getId(), dto.orderId(), dto.sku(), dto.quantity()
        ));

        return ReplacementRequestDto.fromDomain(saved);
    }

    @Transactional
    public ReplacementRequestDto approveReplacement(String replacementRef, String adminNotes, String adminId) {
        ReplacementRequest req = replacementRepository.findByReplacementReference(replacementRef)
                .orElseThrow(() -> new ReplacementNotFoundException(replacementRef));

        // Reserve stock
        try {
            inventoryApplicationService.reserveInventoryForOrder(UUID.fromString(req.getOrderId()), req.getCustomerId());
            req.markInventoryReserved(UUID.randomUUID().toString());
        } catch (Exception e) {
            log.warn("Could not create formal reservation for replacement {}: {}", replacementRef, e.getMessage());
        }

        // Book replacement shipment
        String replacementShipmentId = UUID.randomUUID().toString();
        try {
            shipmentApplicationService.createShipmentForOrder(UUID.fromString(req.getOrderId()));
        } catch (Exception e) {
            log.warn("Could not create formal shipment for replacement {}: {}", replacementRef, e.getMessage());
        }

        req.markShipmentCreated(replacementShipmentId);
        req.approve(adminNotes);
        ReplacementRequest saved = replacementRepository.save(req);

        eventPublisher.publishEvent(ReplacementApprovedEvent.create(
                saved.getId(), saved.getReplacementReference(), saved.getTicketId(), saved.getReservationId(), replacementShipmentId
        ));

        return ReplacementRequestDto.fromDomain(saved);
    }

    @Transactional(readOnly = true)
    public SupportTicketDto getTicketForCustomer(String ticketNumber, String customerId) {
        SupportTicket ticket = getTicketByNumberOrId(ticketNumber);
        validateCustomerOwnership(ticket, customerId);
        return getTicketDtoWithFilter(ticket, true);
    }

    @Transactional(readOnly = true)
    public SupportTicketDto getTicketForAdmin(String ticketNumber) {
        SupportTicket ticket = getTicketByNumberOrId(ticketNumber);
        return getTicketDtoWithFilter(ticket, false);
    }

    @Transactional(readOnly = true)
    public List<SupportTicketDto> listTicketsForCustomer(String customerId) {
        return ticketRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(t -> getTicketDtoWithFilter(t, true))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SupportTicketDto> listTicketsForAdmin(AdminTicketFilterDto filter) {
        return ticketRepository.searchTicketsAdmin(
                filter.status(),
                filter.priority(),
                filter.category(),
                filter.assignedAgentId(),
                filter.searchKey()
        ).stream().map(t -> getTicketDtoWithFilter(t, false)).toList();
    }

    @Transactional
    public void checkSlaBreaches() {
        List<SupportTicket> activeTickets = ticketRepository.findAll().stream()
                .filter(t -> !t.getStatus().isTerminal())
                .toList();

        for (SupportTicket ticket : activeTickets) {
            SlaStatus evaluated = slaCalculationService.evaluateSlaStatus(ticket);
            if (evaluated != ticket.getSlaStatus()) {
                ticket.updateSlaStatus(evaluated);
                ticketRepository.save(ticket);
            }
        }
    }

    private SupportTicket getTicketByNumberOrId(String ref) {
        return ticketRepository.findByTicketNumber(ref)
                .or(() -> ticketRepository.findById(ref))
                .orElseThrow(() -> new TicketNotFoundException(ref));
    }

    private void validateCustomerOwnership(SupportTicket ticket, String customerId) {
        if (!ticket.getCustomerId().equals(customerId)) {
            throw new SupportAccessDeniedException("Customer " + customerId + " does not own ticket " + ticket.getTicketNumber());
        }
    }

    private SupportTicketDto getTicketDtoWithFilter(SupportTicket ticket, boolean isCustomer) {
        List<SupportMessage> messages = isCustomer
                ? messageRepository.findByTicketIdAndVisibilityOrderByCreatedAtAsc(ticket.getId(), MessageVisibility.CUSTOMER_VISIBLE)
                : messageRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId());

        List<SupportMessageDto> messageDtos = messages.stream().map(SupportMessageDto::fromDomain).toList();

        List<ReplacementRequestDto> replacementDtos = replacementRepository.findByTicketId(ticket.getId()).stream()
                .map(ReplacementRequestDto::fromDomain)
                .toList();

        return SupportTicketDto.fromDomain(ticket, messageDtos, replacementDtos);
    }
}
