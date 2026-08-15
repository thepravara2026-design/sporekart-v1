package com.sporekart.modules.support.controller;

import com.sporekart.modules.support.application.SupportApplicationService;
import com.sporekart.modules.support.application.dto.*;
import com.sporekart.modules.support.domain.TicketCategory;
import com.sporekart.modules.support.domain.TicketPriority;
import com.sporekart.modules.support.domain.TicketStatus;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/support")
public class AdminSupportController {

    private static final Logger log = LoggerFactory.getLogger(AdminSupportController.class);

    private final SupportApplicationService supportApplicationService;

    public AdminSupportController(SupportApplicationService supportApplicationService) {
        this.supportApplicationService = supportApplicationService;
    }

    @GetMapping("/tickets")
    public ResponseEntity<List<SupportTicketDto>> listTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) TicketCategory category,
            @RequestParam(required = false) String assignedAgentId,
            @RequestParam(required = false) String searchKey
    ) {
        log.info("REST Admin: List support tickets request status: {}, priority: {}", status, priority);
        AdminTicketFilterDto filter = new AdminTicketFilterDto(status, priority, category, assignedAgentId, searchKey);
        List<SupportTicketDto> results = supportApplicationService.listTicketsForAdmin(filter);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/tickets/{ticketNumber}")
    public ResponseEntity<SupportTicketDto> getAdminTicket(@PathVariable String ticketNumber) {
        log.info("REST Admin: Fetch support ticket: {}", ticketNumber);
        SupportTicketDto result = supportApplicationService.getTicketForAdmin(ticketNumber);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/tickets/{ticketNumber}/messages")
    public ResponseEntity<SupportTicketDto> addAgentMessage(
            @PathVariable String ticketNumber,
            @Valid @RequestBody AddMessageRequestDto requestDto,
            @RequestHeader(value = "X-Admin-Id", defaultValue = "admin-1") String adminId
    ) {
        log.info("REST Admin: Agent reply to ticket: {}", ticketNumber);
        SupportTicketDto result = supportApplicationService.addAgentMessage(ticketNumber, requestDto, adminId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/tickets/{ticketNumber}/assign")
    public ResponseEntity<SupportTicketDto> assignTicket(
            @PathVariable String ticketNumber,
            @Valid @RequestBody AssignTicketRequestDto requestDto,
            @RequestHeader(value = "X-Admin-Id", defaultValue = "admin-1") String adminId
    ) {
        log.info("REST Admin: Assign ticket {} to agent {}", ticketNumber, requestDto.agentId());
        SupportTicketDto result = supportApplicationService.assignTicket(ticketNumber, requestDto.agentId(), adminId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/tickets/{ticketNumber}/priority")
    public ResponseEntity<SupportTicketDto> updatePriority(
            @PathVariable String ticketNumber,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-Admin-Id", defaultValue = "admin-1") String adminId
    ) {
        String priorityStr = body.get("priority");
        TicketPriority priority = TicketPriority.valueOf(priorityStr);
        log.info("REST Admin: Update priority for ticket {} to {}", ticketNumber, priority);
        SupportTicketDto result = supportApplicationService.updatePriority(ticketNumber, priority, adminId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/tickets/{ticketNumber}/escalate")
    public ResponseEntity<SupportTicketDto> escalateTicket(
            @PathVariable String ticketNumber,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-Admin-Id", defaultValue = "admin-1") String adminId
    ) {
        String reason = body != null ? body.getOrDefault("reason", "Escalated by admin") : "Escalated by admin";
        log.info("REST Admin: Escalate ticket: {}", ticketNumber);
        SupportTicketDto result = supportApplicationService.escalateTicket(ticketNumber, reason, adminId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/tickets/{ticketNumber}/resolve")
    public ResponseEntity<SupportTicketDto> resolveTicket(
            @PathVariable String ticketNumber,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-Admin-Id", defaultValue = "admin-1") String adminId
    ) {
        String notes = body != null ? body.get("notes") : null;
        log.info("REST Admin: Resolve ticket: {}", ticketNumber);
        SupportTicketDto result = supportApplicationService.resolveTicket(ticketNumber, notes, adminId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/tickets/{ticketNumber}/close")
    public ResponseEntity<SupportTicketDto> closeTicket(
            @PathVariable String ticketNumber,
            @RequestHeader(value = "X-Admin-Id", defaultValue = "admin-1") String adminId
    ) {
        log.info("REST Admin: Close ticket: {}", ticketNumber);
        SupportTicketDto result = supportApplicationService.closeTicket(ticketNumber, adminId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/replacements/{replacementRef}/approve")
    public ResponseEntity<ReplacementRequestDto> approveReplacement(
            @PathVariable String replacementRef,
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(value = "X-Admin-Id", defaultValue = "admin-1") String adminId
    ) {
        String notes = body != null ? body.get("notes") : "Approved by admin";
        log.info("REST Admin: Approve replacement: {}", replacementRef);
        ReplacementRequestDto result = supportApplicationService.approveReplacement(replacementRef, notes, adminId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/check-sla")
    public ResponseEntity<Map<String, String>> checkSla() {
        log.info("REST Admin: Trigger SLA breach evaluation");
        supportApplicationService.checkSlaBreaches();
        return ResponseEntity.ok(Map.of("status", "SLA evaluation completed"));
    }
}
