package com.sporekart.modules.support.application.dto;

import com.sporekart.modules.support.domain.AuthorType;
import com.sporekart.modules.support.domain.MessageVisibility;
import com.sporekart.modules.support.domain.SupportMessage;

import java.time.OffsetDateTime;

public record SupportMessageDto(
        String id,
        String ticketId,
        String authorId,
        AuthorType authorType,
        MessageVisibility visibility,
        String content,
        String attachmentUrls,
        OffsetDateTime createdAt
) {
    public static SupportMessageDto fromDomain(SupportMessage msg) {
        return new SupportMessageDto(
                msg.getId(),
                msg.getTicketId(),
                msg.getAuthorId(),
                msg.getAuthorType(),
                msg.getVisibility(),
                msg.getContent(),
                msg.getAttachmentUrls(),
                msg.getCreatedAt()
        );
    }
}
