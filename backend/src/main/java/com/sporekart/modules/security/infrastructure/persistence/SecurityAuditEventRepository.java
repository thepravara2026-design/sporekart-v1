package com.sporekart.modules.security.infrastructure.persistence;

import com.sporekart.modules.security.domain.SecurityAuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;

@Repository
public interface SecurityAuditEventRepository extends JpaRepository<SecurityAuditEvent, String> {
    List<SecurityAuditEvent> findByActorIdOrderByCreatedAtDesc(String actorId);
    Page<SecurityAuditEvent> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT a FROM SecurityAuditEvent a WHERE a.createdAt <= :cutoff ORDER BY a.createdAt ASC")
    Page<SecurityAuditEvent> findEventsOlderThan(@Param("cutoff") Instant cutoff, Pageable pageable);

    @Query("SELECT COUNT(a) FROM SecurityAuditEvent a WHERE a.createdAt <= :cutoff")
    long countEventsOlderThan(@Param("cutoff") Instant cutoff);
}
