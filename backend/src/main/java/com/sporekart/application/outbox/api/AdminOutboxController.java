package com.sporekart.application.outbox.api;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.application.outbox.application.OutboxOperationsService;
import com.sporekart.application.outbox.domain.OutboxStatus;
import com.sporekart.application.outbox.dto.OutboxEventDetailResponse;
import com.sporekart.application.outbox.dto.OutboxEventSummaryResponse;
import com.sporekart.application.outbox.dto.OutboxHealthResponse;
import com.sporekart.application.outbox.dto.OutboxReplayRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/outbox")
@PreAuthorize("hasAnyRole('ADMIN', 'ROLE_ADMIN')")
public class AdminOutboxController {

    private final OutboxOperationsService outboxOperationsService;

    public AdminOutboxController(OutboxOperationsService outboxOperationsService) {
        this.outboxOperationsService = outboxOperationsService;
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<OutboxHealthResponse>> getHealth() {
        OutboxHealthResponse health = outboxOperationsService.getOutboxHealth();
        return ResponseEntity.ok(ApiResponse.success(health));
    }

    @GetMapping("/events")
    public ResponseEntity<ApiResponse<Page<OutboxEventSummaryResponse>>> getEvents(
            @RequestParam(required = false) OutboxStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<OutboxEventSummaryResponse> events = outboxOperationsService.findEvents(status, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<ApiResponse<OutboxEventDetailResponse>> getEventDetail(@PathVariable String eventId) {
        OutboxEventDetailResponse detail = outboxOperationsService.getEventDetail(eventId);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    @PostMapping("/events/{eventId}/replay")
    public ResponseEntity<ApiResponse<OutboxEventDetailResponse>> replayEvent(
            @PathVariable String eventId,
            @RequestBody(required = false) OutboxReplayRequest request,
            Authentication authentication
    ) {
        String requestedBy = authentication != null ? authentication.getName() : "ADMIN";
        String reason = request != null ? request.reason() : "Operational replay request";
        OutboxEventDetailResponse replayed = outboxOperationsService.replayDeadLetterEvent(eventId, requestedBy, reason);
        return ResponseEntity.ok(ApiResponse.success(replayed));
    }

    @PostMapping("/stale/recover")
    public ResponseEntity<ApiResponse<Map<String, Object>>> recoverStaleProcessing(
            @RequestParam(defaultValue = "5") int timeoutMinutes
    ) {
        int count = outboxOperationsService.recoverStaleProcessingEvents(timeoutMinutes);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "recoveredCount", count,
                "timeoutMinutes", timeoutMinutes,
                "status", "SUCCESS"
        )));
    }
}
