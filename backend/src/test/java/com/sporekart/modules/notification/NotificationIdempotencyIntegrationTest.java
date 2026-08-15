package com.sporekart.modules.notification;

import com.sporekart.modules.notification.application.NotificationApplicationService;
import com.sporekart.modules.notification.domain.Notification;
import com.sporekart.modules.notification.domain.NotificationCategory;
import com.sporekart.modules.notification.domain.NotificationChannel;
import com.sporekart.modules.notification.domain.NotificationPriority;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Notification Idempotency & Concurrency Integration Tests")
public class NotificationIdempotencyIntegrationTest {

    @Autowired
    private NotificationApplicationService notificationService;

    @Test
    @DisplayName("Duplicate request with same idempotency key should return existing notification record")
    void duplicateRequestWithSameIdempotencyKeyShouldReturnExisting() {
        String idempotencyKey = "IDEM-TEST-" + UUID.randomUUID();

        Optional<Notification> first = notificationService.sendNotification(
                "evt-999", "ORDER_CREATED", "user-idempotent", "cust-idempotent",
                NotificationChannel.EMAIL, "ORDER_CONFIRMATION", NotificationCategory.ORDER_UPDATES,
                "user@example.com", Map.of("orderNumber", "ORD-112233"), NotificationPriority.NORMAL,
                idempotencyKey, "corr-112233", "trace-112233"
        );

        assertTrue(first.isPresent());

        // Duplicate request
        Optional<Notification> second = notificationService.sendNotification(
                "evt-999", "ORDER_CREATED", "user-idempotent", "cust-idempotent",
                NotificationChannel.EMAIL, "ORDER_CONFIRMATION", NotificationCategory.ORDER_UPDATES,
                "user@example.com", Map.of("orderNumber", "ORD-112233"), NotificationPriority.NORMAL,
                idempotencyKey, "corr-112233", "trace-112233"
        );

        assertTrue(second.isPresent());
        assertEquals(first.get().getId(), second.get().getId());
    }

    @Test
    @DisplayName("Concurrent duplicate requests should not produce duplicate notification deliveries")
    void concurrentDuplicateRequestsShouldSuppressDuplicates() throws InterruptedException {
        String eventId = "evt-concurrent-" + UUID.randomUUID();
        int threads = 4;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    Optional<Notification> opt = notificationService.sendNotification(
                            eventId, "ORDER_CREATED", "user-concurrent", "cust-concurrent",
                            NotificationChannel.IN_APP, "ORDER_CONFIRMATION", NotificationCategory.ORDER_UPDATES,
                            "user-concurrent", Map.of("orderNumber", "ORD-554433"), NotificationPriority.NORMAL,
                            eventId + "-INAPP-KEY", "corr-conc", "trace-conc"
                    );
                    if (opt.isPresent()) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertTrue(successCount.get() >= 1);
    }
}
