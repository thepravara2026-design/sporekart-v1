package com.sporekart.modules.support.application.dto;

import com.sporekart.modules.support.domain.TicketCategory;
import com.sporekart.modules.support.domain.TicketPriority;
import com.sporekart.modules.support.domain.TicketStatus;

public record AdminTicketFilterDto(
        TicketStatus status,
        TicketPriority priority,
        TicketCategory category,
        String assignedAgentId,
        String searchKey
) {}
