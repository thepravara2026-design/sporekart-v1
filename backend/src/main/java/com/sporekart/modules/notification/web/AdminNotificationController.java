package com.sporekart.modules.notification.web;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.notification.application.NotificationApplicationService;
import com.sporekart.modules.notification.application.NotificationOperationsService;
import com.sporekart.modules.notification.application.NotificationTemplateService;
import com.sporekart.modules.notification.domain.*;
import com.sporekart.modules.security.domain.SecurityAuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'ROLE_ADMIN')")
public class AdminNotificationController {

    private final NotificationApplicationService notificationService;
    private final NotificationOperationsService operationsService;
    private final NotificationTemplateService templateService;

    public AdminNotificationController(NotificationApplicationService notificationService,
                                       NotificationOperationsService operationsService,
                                       NotificationTemplateService templateService) {
        this.notificationService = notificationService;
        this.operationsService = operationsService;
        this.templateService = templateService;
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<Page<NotificationOperationsService.NotificationDetailDto>>> getNotifications(
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) NotificationChannel channel,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationOperationsService.NotificationDetailDto> notifications =
                operationsService.getFilteredNotifications(status, channel, provider, fromDate, toDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/notifications/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNotificationHealth() {
        Map<String, Object> health = operationsService.getOverallHealthSummary();
        return ResponseEntity.ok(ApiResponse.success(health));
    }

    @GetMapping("/notifications/providers")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProvidersHealth() {
        Map<String, Object> health = operationsService.getOverallHealthSummary();
        Map<String, Object> resilience = (Map<String, Object>) health.get("resilience");
        return ResponseEntity.ok(ApiResponse.success(resilience != null ? resilience : Map.of()));
    }

    @GetMapping("/notifications/providers/{provider}/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSingleProviderHealth(@PathVariable String provider) {
        Map<String, Object> health = operationsService.getOverallHealthSummary();
        Map<String, Object> resilience = (Map<String, Object>) health.get("resilience");

        if (resilience != null && resilience.containsKey("providers")) {
            List<Map<String, Object>> providers = (List<Map<String, Object>>) resilience.get("providers");
            for (Map<String, Object> p : providers) {
                String name = (String) p.get("name");
                if (provider.equalsIgnoreCase(name)) {
                    return ResponseEntity.ok(ApiResponse.success(p));
                }
            }
        }
        return ResponseEntity.ok(ApiResponse.success(Map.of("name", provider, "status", "UNKNOWN")));
    }

    @GetMapping("/notifications/backlog")
    public ResponseEntity<ApiResponse<NotificationOperationsService.BacklogSummaryDto>> getBacklogSummary() {
        NotificationOperationsService.BacklogSummaryDto backlog = operationsService.getBacklogSummary();
        return ResponseEntity.ok(ApiResponse.success(backlog));
    }

    @GetMapping("/notifications/reconciliation")
    public ResponseEntity<ApiResponse<Page<NotificationOperationsService.NotificationDetailDto>>> getReconciliationBacklog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationOperationsService.NotificationDetailDto> backlog = operationsService.getReconciliationBacklog(pageable);
        return ResponseEntity.ok(ApiResponse.success(backlog));
    }

    @GetMapping("/notifications/audit")
    public ResponseEntity<ApiResponse<Page<SecurityAuditEvent>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SecurityAuditEvent> logs = operationsService.getAuditLogs(pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/notifications/{id}")
    public ResponseEntity<ApiResponse<NotificationOperationsService.NotificationDetailDto>> getNotificationDetail(@PathVariable String id) {
        NotificationOperationsService.NotificationDetailDto detail = operationsService.getNotificationDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    @GetMapping("/notifications/{id}/timeline")
    public ResponseEntity<ApiResponse<List<NotificationOperationsService.NotificationTimelineEventDto>>> getNotificationTimeline(@PathVariable String id) {
        List<NotificationOperationsService.NotificationTimelineEventDto> timeline = operationsService.getNotificationTimeline(id);
        return ResponseEntity.ok(ApiResponse.success(timeline));
    }

    @PostMapping("/notifications/{id}/retry")
    public ResponseEntity<ApiResponse<NotificationOperationsService.NotificationDetailDto>> retryNotification(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable String id) {
        String adminUser = principal != null ? principal.getUsername() : "ADMIN";
        NotificationOperationsService.NotificationDetailDto detail = operationsService.adminRetry(adminUser, id);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    @PostMapping("/notifications/{id}/cancel")
    public ResponseEntity<ApiResponse<NotificationOperationsService.NotificationDetailDto>> cancelNotification(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> body) {
        String adminUser = principal != null ? principal.getUsername() : "ADMIN";
        String reason = body != null ? body.get("reason") : "Cancelled by admin request";
        NotificationOperationsService.NotificationDetailDto detail = operationsService.adminCancel(adminUser, id, reason);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    @PostMapping("/notifications/stale-recover")
    public ResponseEntity<ApiResponse<Map<String, Object>>> recoverStaleProcessing(@AuthenticationPrincipal UserDetails principal) {
        String adminUser = principal != null ? principal.getUsername() : "ADMIN";
        int count = operationsService.triggerStaleRecovery(adminUser);
        return ResponseEntity.ok(ApiResponse.success(Map.of("recoveredCount", count)));
    }

    @GetMapping("/notifications/{id}/attempts")
    public ResponseEntity<ApiResponse<List<NotificationDeliveryAttempt>>> getDeliveryAttempts(@PathVariable String id) {
        List<NotificationDeliveryAttempt> attempts = notificationService.getDeliveryAttempts(id);
        return ResponseEntity.ok(ApiResponse.success(attempts));
    }

    @GetMapping("/notification-templates")
    public ResponseEntity<ApiResponse<List<NotificationTemplate>>> getAllTemplates() {
        List<NotificationTemplate> templates = templateService.getAllTemplates();
        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    @PostMapping("/notification-templates")
    public ResponseEntity<ApiResponse<NotificationTemplate>> createTemplate(@RequestBody CreateTemplateRequest request) {
        NotificationTemplate template = templateService.createTemplate(
                request.templateCode(), request.name(), request.description(), request.category()
        );
        return ResponseEntity.ok(ApiResponse.success(template));
    }

    @GetMapping("/notification-templates/{templateCode}/versions")
    public ResponseEntity<ApiResponse<List<NotificationTemplateVersion>>> getVersions(@PathVariable String templateCode) {
        List<NotificationTemplateVersion> versions = templateService.getVersionsForTemplate(templateCode);
        return ResponseEntity.ok(ApiResponse.success(versions));
    }

    @PostMapping("/notification-templates/{templateCode}/versions")
    public ResponseEntity<ApiResponse<NotificationTemplateVersion>> createVersion(
            @PathVariable String templateCode,
            @RequestBody CreateVersionRequest request) {
        NotificationTemplateVersion version = templateService.createTemplateVersion(
                templateCode, request.channel(), request.version(),
                request.locale(), request.subject(), request.body()
        );
        return ResponseEntity.ok(ApiResponse.success(version));
    }

    @PostMapping("/notification-template-versions/{versionId}/activate")
    public ResponseEntity<ApiResponse<NotificationTemplateVersion>> activateVersion(@PathVariable String versionId) {
        NotificationTemplateVersion version = templateService.activateTemplateVersion(versionId);
        return ResponseEntity.ok(ApiResponse.success(version));
    }

    public record CreateTemplateRequest(
            String templateCode,
            String name,
            String description,
            NotificationCategory category
    ) {}

    public record CreateVersionRequest(
            NotificationChannel channel,
            int version,
            String locale,
            String subject,
            String body
    ) {}
}
