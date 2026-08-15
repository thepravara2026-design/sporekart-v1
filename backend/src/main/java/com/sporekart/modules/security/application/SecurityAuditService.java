package com.sporekart.modules.security.application;

import com.sporekart.modules.security.domain.AuditEventType;
import com.sporekart.modules.security.domain.AuditStatus;
import com.sporekart.modules.security.domain.SecurityAuditEvent;
import com.sporekart.modules.security.infrastructure.persistence.SecurityAuditEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SecurityAuditService {

    private static final Logger log = LoggerFactory.getLogger(SecurityAuditService.class);

    private final SecurityAuditEventRepository auditEventRepository;

    public SecurityAuditService(SecurityAuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @Transactional
    public void logEvent(AuditEventType eventType, String actorId, String targetId, String ipAddress, String userAgent, AuditStatus status, String details) {
        try {
            SecurityAuditEvent event = new SecurityAuditEvent(eventType, actorId, targetId, ipAddress, userAgent, status, details);
            auditEventRepository.save(event);
            log.info("SECURITY_AUDIT: [{}] actor: {}, target: {}, status: {}", eventType, actorId, targetId, status);
        } catch (Exception ex) {
            log.error("Failed to log security audit event: {}", ex.getMessage(), ex);
        }
    }
}
