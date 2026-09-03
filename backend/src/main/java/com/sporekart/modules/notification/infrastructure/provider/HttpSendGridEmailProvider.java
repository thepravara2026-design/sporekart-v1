package com.sporekart.modules.notification.infrastructure.provider;

import com.sporekart.modules.notification.domain.Notification;
import com.sporekart.modules.notification.domain.NotificationChannel;
import com.sporekart.modules.notification.infrastructure.config.NotificationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class HttpSendGridEmailProvider implements NotificationProvider {

    private static final Logger log = LoggerFactory.getLogger(HttpSendGridEmailProvider.class);

    private final NotificationProperties properties;
    private final MockEmailProvider mockEmailProvider;
    private final RestTemplate restTemplate;

    private String baseUrlOverride;

    public HttpSendGridEmailProvider(NotificationProperties properties,
                                     MockEmailProvider mockEmailProvider,
                                     @Autowired(required = false) RestTemplate restTemplate) {
        this.properties = properties;
        this.mockEmailProvider = mockEmailProvider;
        this.restTemplate = restTemplate != null ? restTemplate : new RestTemplate();
    }

    public void setBaseUrlOverride(String baseUrlOverride) {
        this.baseUrlOverride = baseUrlOverride;
    }

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.EMAIL;
    }

    @Override
    public String getProviderName() {
        return properties.getEmail().isRealMode() ? "HttpSendGridEmailProvider" : mockEmailProvider.getProviderName();
    }

    @Override
    public NotificationProviderResult send(Notification notification) {
        if (!properties.getEmail().isRealMode()) {
            return mockEmailProvider.send(notification);
        }

        String apiKey = properties.getEmail().getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("SendGrid API key missing, failing notification permanently");
            return NotificationProviderResult.failure("SendGrid API key missing", false);
        }

        String endpoint = (baseUrlOverride != null ? baseUrlOverride : "https://api.sendgrid.com") + "/v3/mail/send";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> payload = Map.of(
                "personalizations", List.of(Map.of("to", List.of(Map.of("email", notification.getRecipient())))),
                "from", Map.of("email", properties.getEmail().getFromEmail(), "name", properties.getEmail().getFromName()),
                "subject", notification.getSubject() != null ? notification.getSubject() : "Notification",
                "content", List.of(Map.of("type", "text/plain", "value", notification.getBody()))
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.POST, request, String.class);
            String messageId = response.getHeaders().getFirst("X-Message-Id");
            if (messageId == null || messageId.isBlank()) {
                messageId = "sg-" + UUID.randomUUID();
            }
            log.info("SendGrid email delivered successfully to '{}', messageId: {}", notification.getRecipient(), messageId);
            return NotificationProviderResult.success(messageId);
        } catch (HttpClientErrorException ex) {
            log.error("SendGrid HTTP 4xx error for recipient '{}': {}", notification.getRecipient(), ex.getMessage());
            boolean isTransient = ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS;
            return NotificationProviderResult.failure("SendGrid Client Error: " + ex.getStatusCode(), isTransient);
        } catch (HttpServerErrorException ex) {
            log.error("SendGrid HTTP 5xx error for recipient '{}': {}", notification.getRecipient(), ex.getMessage());
            return NotificationProviderResult.failure("SendGrid Server Error: " + ex.getStatusCode(), true);
        } catch (ResourceAccessException ex) {
            log.error("SendGrid timeout/network error for recipient '{}': {}", notification.getRecipient(), ex.getMessage());
            return NotificationProviderResult.failure("SendGrid Timeout: " + ex.getMessage(), true);
        } catch (Exception ex) {
            log.error("SendGrid unknown error for recipient '{}': {}", notification.getRecipient(), ex.getMessage());
            return NotificationProviderResult.failure("SendGrid Error: " + ex.getMessage(), true);
        }
    }
}
