package com.sporekart.modules.support.application.dto;

import com.sporekart.modules.support.domain.IssueType;
import com.sporekart.modules.support.domain.TicketCategory;
import com.sporekart.modules.support.domain.TicketPriority;
import com.sporekart.modules.support.domain.TicketSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTicketRequestDto(
        String orderId,
        String orderReference,

        @NotNull(message = "Category is required")
        TicketCategory category,

        @NotNull(message = "Issue type is required")
        IssueType issueType,

        TicketPriority priority,
        TicketSource source,

        @NotBlank(message = "Subject is required")
        @Size(max = 255, message = "Subject cannot exceed 255 characters")
        String subject,

        @NotBlank(message = "Description is required")
        String description,

        String attachmentUrls
) {}
