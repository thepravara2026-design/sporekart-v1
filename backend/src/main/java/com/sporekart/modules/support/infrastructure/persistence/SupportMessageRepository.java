package com.sporekart.modules.support.infrastructure.persistence;

import com.sporekart.modules.support.domain.MessageVisibility;
import com.sporekart.modules.support.domain.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportMessageRepository extends JpaRepository<SupportMessage, String> {

    List<SupportMessage> findByTicketIdOrderByCreatedAtAsc(String ticketId);

    List<SupportMessage> findByTicketIdAndVisibilityOrderByCreatedAtAsc(String ticketId, MessageVisibility visibility);
}
