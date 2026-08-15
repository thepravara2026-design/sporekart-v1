package com.sporekart.modules.support.infrastructure.persistence;

import com.sporekart.modules.support.domain.ReplacementRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReplacementRequestRepository extends JpaRepository<ReplacementRequest, String> {

    Optional<ReplacementRequest> findByReplacementReference(String replacementReference);

    List<ReplacementRequest> findByTicketId(String ticketId);

    List<ReplacementRequest> findByCustomerId(String customerId);

    @Query("SELECT COUNT(r) FROM ReplacementRequest r WHERE r.replacementReference LIKE CONCAT('RPL-', :year, '-%')")
    long countReplacementsForYear(@Param("year") String year);
}
