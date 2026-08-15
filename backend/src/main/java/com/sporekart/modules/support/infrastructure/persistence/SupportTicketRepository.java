package com.sporekart.modules.support.infrastructure.persistence;

import com.sporekart.modules.support.domain.SupportTicket;
import com.sporekart.modules.support.domain.TicketCategory;
import com.sporekart.modules.support.domain.TicketPriority;
import com.sporekart.modules.support.domain.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, String> {

    Optional<SupportTicket> findByTicketNumber(String ticketNumber);

    List<SupportTicket> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    @Query("SELECT t FROM SupportTicket t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:category IS NULL OR t.category = :category) AND " +
           "(:assignedAgentId IS NULL OR t.assignedAgentId = :assignedAgentId) AND " +
           "(:searchKey IS NULL OR LOWER(t.ticketNumber) LIKE LOWER(CONCAT('%', :searchKey, '%')) OR LOWER(t.subject) LIKE LOWER(CONCAT('%', :searchKey, '%')) OR LOWER(t.customerId) LIKE LOWER(CONCAT('%', :searchKey, '%'))) " +
           "ORDER BY t.createdAt DESC")
    List<SupportTicket> searchTicketsAdmin(
            @Param("status") TicketStatus status,
            @Param("priority") TicketPriority priority,
            @Param("category") TicketCategory category,
            @Param("assignedAgentId") String assignedAgentId,
            @Param("searchKey") String searchKey
    );

    @Query("SELECT COUNT(t) FROM SupportTicket t WHERE t.ticketNumber LIKE CONCAT('TKT-', :year, '-%')")
    long countTicketsForYear(@Param("year") String year);
}
