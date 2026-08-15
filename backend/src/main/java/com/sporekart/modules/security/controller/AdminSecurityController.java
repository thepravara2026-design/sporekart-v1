package com.sporekart.modules.security.controller;

import com.sporekart.modules.security.application.AuthenticationApplicationService;
import com.sporekart.modules.security.application.dto.SecurityAuditEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/security")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSecurityController {

    private static final Logger log = LoggerFactory.getLogger(AdminSecurityController.class);

    private final AuthenticationApplicationService authService;

    public AdminSecurityController(AuthenticationApplicationService authService) {
        this.authService = authService;
    }

    @PostMapping("/users/{userId}/lock")
    public ResponseEntity<Void> lockUserAccount(
            @PathVariable String userId,
            @RequestParam(defaultValue = "60") long durationMinutes
    ) {
        log.info("REST Admin: Lock user account: {} for {} minutes", userId, durationMinutes);
        authService.lockUserAccount(userId, durationMinutes);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{userId}/unlock")
    public ResponseEntity<Void> unlockUserAccount(@PathVariable String userId) {
        log.info("REST Admin: Unlock user account: {}", userId);
        authService.unlockUserAccount(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/audit-events")
    public ResponseEntity<Page<SecurityAuditEventDto>> getAuditEvents(Pageable pageable) {
        log.info("REST Admin: Fetch security audit events");
        Page<SecurityAuditEventDto> events = authService.getAuditEvents(pageable);
        return ResponseEntity.ok(events);
    }
}
