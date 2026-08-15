package com.sporekart.modules.notification.web;

import com.sporekart.modules.notification.application.NotificationApplicationService;
import com.sporekart.modules.notification.domain.Notification;
import com.sporekart.modules.notification.domain.NotificationCategory;
import com.sporekart.modules.notification.domain.NotificationChannel;
import com.sporekart.modules.notification.domain.NotificationPriority;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Notification REST API Security & Customer Isolation Tests")
public class NotificationSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationApplicationService notificationService;

    @Test
    @DisplayName("Unauthenticated request to GET /api/v1/notifications should return 401 Unauthorized")
    void unauthenticatedGetNotificationsShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "customer-user-1", roles = "CUSTOMER")
    @DisplayName("Authenticated customer should list only their notifications")
    void authenticatedCustomerShouldListTheirNotifications() throws Exception {
        notificationService.sendNotification(
                "evt-sec-1", "ORDER_CREATED", "customer-user-1", "cust-sec-1",
                NotificationChannel.IN_APP, "ORDER_CONFIRMATION", NotificationCategory.ORDER_UPDATES,
                "customer-user-1", Map.of("orderNumber", "ORD-SEC-01"), NotificationPriority.NORMAL,
                "idem-sec-1-" + UUID.randomUUID(), "corr-sec", "trace-sec"
        );

        mockMvc.perform(get("/api/v1/notifications/in-app"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @WithMockUser(username = "attacker-user", roles = "CUSTOMER")
    @DisplayName("User trying to mark another user's notification as read should be rejected")
    void markAsReadByDifferentUserShouldBeRejected() {
        Optional<Notification> opt = notificationService.sendNotification(
                "evt-sec-2", "ORDER_CREATED", "victim-user", "cust-sec-2",
                NotificationChannel.IN_APP, "ORDER_CONFIRMATION", NotificationCategory.ORDER_UPDATES,
                "victim-user", Map.of("orderNumber", "ORD-SEC-02"), NotificationPriority.NORMAL,
                "idem-sec-2-" + UUID.randomUUID(), "corr-sec", "trace-sec"
        );

        assertTrue(opt.isPresent());
        String victimNotificationId = opt.get().getId();

        assertThrows(SecurityException.class, () ->
                notificationService.markAsRead("attacker-user", victimNotificationId)
        );
    }

    @Test
    @WithMockUser(username = "regular-user", roles = "CUSTOMER")
    @DisplayName("Non-admin user accessing admin notification endpoints should return 403 Forbidden")
    void nonAdminAccessingAdminEndpointsShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/notifications"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin-user", roles = "ADMIN")
    @DisplayName("Admin user accessing admin notification endpoints should return 200 OK")
    void adminAccessingAdminEndpointsShouldSucceed() throws Exception {
        mockMvc.perform(get("/api/v1/admin/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
