package com.sporekart.modules.security.infrastructure.persistence;

import com.sporekart.modules.security.domain.SecurityAuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecurityAuditEventRepository extends JpaRepository<SecurityAuditEvent, String> {
    List<SecurityAuditEvent> findByActorIdOrderByCreatedAtDesc(String actorId);
    Page<SecurityAuditEvent> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
