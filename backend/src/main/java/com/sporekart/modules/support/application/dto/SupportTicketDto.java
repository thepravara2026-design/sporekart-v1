package com.sporekart.modules.support.application.dto;

import com.sporekart.modules.support.domain.*;

import java.time.OffsetDateTime;
import java.util.List;

public record SupportTicketDto(
        String id,
        String ticketNumber,
        String customerId,
        String orderId,
        String orderReference,
        TicketCategory category,
        IssueType issueType,
        TicketStatus status,
        TicketPriority priority,
        TicketSource source,
        String subject,
        String description,
        String assignedAgentId,
        OffsetDateTime firstResponseAt,
        OffsetDateTime firstResponseDueAt,
        OffsetDateTime resolutionDueAt,
        SlaStatus slaStatus,
        OffsetDateTime resolvedAt,
        OffsetDateTime closedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<SupportMessageDto> messages,
        List<ReplacementRequestDto> replacementRequests
) {
    public static SupportTicketDto fromDomain(
            SupportTicket ticket,
            List<SupportMessageDto> messages,
            List<ReplacementRequestDto> replacements
    ) {
        return new SupportTicketDto(
                ticket.getId(),
                ticket.getTicketNumber(),
                ticket.getCustomerId(),
                ticket.getOrderId(),
                ticket.getOrderReference(),
                ticket.getCategory(),
                ticket.getIssueType(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getSource(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getAssignedAgentId(),
                ticket.getFirstResponseAt(),
                ticket.getFirstResponseDueAt(),
                ticket.getResolutionDueAt(),
                ticket.getSlaStatus(),
                ticket.getResolvedAt(),
                ticket.getClosedAt(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                messages != null ? messages : List.of(),
                replacements != null ? replacements : List.of()
        );
    }
}
