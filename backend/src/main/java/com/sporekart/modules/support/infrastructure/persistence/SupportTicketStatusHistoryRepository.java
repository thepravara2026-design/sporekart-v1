package com.sporekart.modules.support.infrastructure.persistence;

import com.sporekart.modules.support.domain.SupportTicketStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportTicketStatusHistoryRepository extends JpaRepository<SupportTicketStatusHistory, String> {

    List<SupportTicketStatusHistory> findByTicketIdOrderByCreatedAtAsc(String ticketId);
}
