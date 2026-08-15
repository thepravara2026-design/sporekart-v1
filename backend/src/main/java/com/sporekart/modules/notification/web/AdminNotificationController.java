package com.sporekart.modules.notification.web;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.notification.application.NotificationApplicationService;
import com.sporekart.modules.notification.application.NotificationTemplateService;
import com.sporekart.modules.notification.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminNotificationController {

    private final NotificationApplicationService notificationService;
    private final NotificationTemplateService templateService;

    public AdminNotificationController(NotificationApplicationService notificationService,
                                       NotificationTemplateService templateService) {
        this.notificationService = notificationService;
        this.templateService = templateService;
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<Page<Notification>>> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationService.getAllNotificationsAdmin(pageable);
        return ResponseEntity.ok(ApiResponse.success(notifications));
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
