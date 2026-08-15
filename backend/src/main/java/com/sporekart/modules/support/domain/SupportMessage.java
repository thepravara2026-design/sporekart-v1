package com.sporekart.modules.support.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "support_messages")
public class SupportMessage {

    @Id
    private String id;

    @Column(name = "ticket_id", nullable = false, length = 36)
    private String ticketId;

    @Column(name = "author_id", nullable = false, length = 64)
    private String authorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "author_type", nullable = false, length = 16)
    private AuthorType authorType;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 32)
    private MessageVisibility visibility;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "attachment_urls", columnDefinition = "TEXT")
    private String attachmentUrls;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected SupportMessage() {}

    public static SupportMessage create(
            String ticketId,
            String authorId,
            AuthorType authorType,
            MessageVisibility visibility,
            String content,
            String attachmentUrls
    ) {
        SupportMessage message = new SupportMessage();
        message.id = UUID.randomUUID().toString();
        message.ticketId = ticketId;
        message.authorId = authorId;
        message.authorType = authorType;
        message.visibility = visibility != null ? visibility : MessageVisibility.CUSTOMER_VISIBLE;
        message.content = content;
        message.attachmentUrls = attachmentUrls;
        message.createdAt = OffsetDateTime.now();
        return message;
    }

    public String getId() { return id; }
    public String getTicketId() { return ticketId; }
    public String getAuthorId() { return authorId; }
    public AuthorType getAuthorType() { return authorType; }
    public MessageVisibility getVisibility() { return visibility; }
    public String getContent() { return content; }
    public String getAttachmentUrls() { return attachmentUrls; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
