package com.sporekart.modules.notification.web;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.notification.application.NotificationWebhookService;
import com.sporekart.modules.notification.application.NotificationWebhookService.WebhookProcessingResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/webhooks/notifications")
public class NotificationWebhookController {

    private final NotificationWebhookService webhookService;

    public NotificationWebhookController(NotificationWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/{provider}")
    public ResponseEntity<ApiResponse<Object>> handleProviderWebhook(
            @PathVariable String provider,
            @RequestHeader(value = "X-Signature", required = false) String genericSig,
            @RequestHeader(value = "X-Twilio-Signature", required = false) String twilioSig,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String metaSig,
            @RequestBody Map<String, Object> body) {

        String signature = twilioSig != null ? twilioSig : (metaSig != null ? metaSig : genericSig);

        String providerMessageId = body.get("providerMessageId") != null ? body.get("providerMessageId").toString() :
                (body.get("MessageSid") != null ? body.get("MessageSid").toString() :
                (body.get("id") != null ? body.get("id").toString() : null));

        String providerEventId = body.get("providerEventId") != null ? body.get("providerEventId").toString() :
                (body.get("eventId") != null ? body.get("eventId").toString() : null);

        String providerStatus = body.get("providerStatus") != null ? body.get("providerStatus").toString() :
                (body.get("SmsStatus") != null ? body.get("SmsStatus").toString() :
                (body.get("status") != null ? body.get("status").toString() : null));

        String rawPayload = body.toString();

        WebhookProcessingResult result = webhookService.processDeliveryCallback(
                provider, providerMessageId, providerEventId, providerStatus, signature, rawPayload
        );

        if (result.unauthorized()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, Map.of("error", result.message(), "code", "UNAUTHORIZED")));
        }

        if (!result.success() && !result.duplicate()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, Map.of("error", result.message(), "code", "NOT_FOUND")));
        }

        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "notificationId", result.notificationId() != null ? result.notificationId() : "",
                "status", result.status() != null ? result.status().name() : "PROCESSED",
                "message", result.message()
        )));
    }
}
