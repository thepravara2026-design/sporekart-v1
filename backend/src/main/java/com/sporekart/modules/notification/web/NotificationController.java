package com.sporekart.modules.notification.web;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.notification.application.NotificationApplicationService;
import com.sporekart.modules.notification.application.NotificationPreferenceService;
import com.sporekart.modules.notification.domain.Notification;
import com.sporekart.modules.notification.domain.NotificationCategory;
import com.sporekart.modules.notification.domain.NotificationPreference;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Notifications", description = "Customer notification inbox, mark-as-read operations, and channel preference management")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationApplicationService notificationService;
    private final NotificationPreferenceService preferenceService;

    public NotificationController(NotificationApplicationService notificationService,
                                  NotificationPreferenceService preferenceService) {
        this.notificationService = notificationService;
        this.preferenceService = preferenceService;
    }

    @GetMapping("/notifications")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<NotificationDto>>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String currentUserId = getCurrentUserId();
        int safeSize = Math.min(Math.max(1, size), 50);
        int safePage = Math.max(0, page);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<Notification> notifications = notificationService.getUserNotifications(currentUserId, pageable);
        return ResponseEntity.ok(ApiResponse.success(notifications.map(NotificationDto::fromDomain)));
    }

    @GetMapping("/notifications/in-app")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<NotificationDto>>> getInAppNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String currentUserId = getCurrentUserId();
        int safeSize = Math.min(Math.max(1, size), 50);
        int safePage = Math.max(0, page);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<Notification> notifications = notificationService.getUserInAppNotifications(currentUserId, pageable);
        return ResponseEntity.ok(ApiResponse.success(notifications.map(NotificationDto::fromDomain)));
    }

    @GetMapping("/notifications/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount() {
        String currentUserId = getCurrentUserId();
        long count = notificationService.getUnreadInAppCount(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("unreadCount", count)));
    }

    @PostMapping("/notifications/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NotificationDto>> markAsRead(@PathVariable String id) {
        String currentUserId = getCurrentUserId();
        Notification updated = notificationService.markAsRead(currentUserId, id);
        return ResponseEntity.ok(ApiResponse.success(NotificationDto.fromDomain(updated)));
    }

    @PostMapping("/notifications/mark-all-read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, String>>> markAllAsRead() {
        String currentUserId = getCurrentUserId();
        notificationService.markAllAsRead(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "All notifications marked as read")));
    }

    @GetMapping("/notification-preferences")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<NotificationPreference>>> getPreferences() {
        String currentUserId = getCurrentUserId();
        List<NotificationPreference> preferences = preferenceService.getUserPreferences(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(preferences));
    }

    @PutMapping("/notification-preferences")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NotificationPreference>> updatePreference(
            @RequestBody PreferenceUpdateRequest request) {
        String currentUserId = getCurrentUserId();
        NotificationPreference updated = preferenceService.updatePreference(
                currentUserId, request.category(),
                request.emailEnabled(), request.smsEnabled(),
                request.whatsappEnabled(), request.inAppEnabled()
        );
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }
        return auth.getName();
    }

    public record NotificationDto(
            String id, String eventType, String channel, String subject, String body,
            String status, String priority, boolean read, String createdAt, String deliveredAt
    ) {
        public static NotificationDto fromDomain(Notification n) {
            return new NotificationDto(
                    n.getId(), n.getEventType(), n.getChannel().name(), n.getSubject(), n.getBody(),
                    n.getStatus().name(), n.getPriority().name(), n.getReadAt() != null,
                    n.getCreatedAt().toString(), n.getDeliveredAt() != null ? n.getDeliveredAt().toString() : null
            );
        }
    }

    public record PreferenceUpdateRequest(
            NotificationCategory category,
            boolean emailEnabled,
            boolean smsEnabled,
            boolean whatsappEnabled,
            boolean inAppEnabled
    ) {}
}
