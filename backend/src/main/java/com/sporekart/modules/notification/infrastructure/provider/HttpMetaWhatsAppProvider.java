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
public class HttpMetaWhatsAppProvider implements NotificationProvider {

    private static final Logger log = LoggerFactory.getLogger(HttpMetaWhatsAppProvider.class);

    private final NotificationProperties properties;
    private final MockWhatsAppProvider mockWhatsAppProvider;
    private final RestTemplate restTemplate;

    private String baseUrlOverride;

    public HttpMetaWhatsAppProvider(NotificationProperties properties,
                                    MockWhatsAppProvider mockWhatsAppProvider,
                                    @Autowired(required = false) RestTemplate restTemplate) {
        this.properties = properties;
        this.mockWhatsAppProvider = mockWhatsAppProvider;
        this.restTemplate = restTemplate != null ? restTemplate : new RestTemplate();
    }

    public void setBaseUrlOverride(String baseUrlOverride) {
        this.baseUrlOverride = baseUrlOverride;
    }

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.WHATSAPP;
    }

    @Override
    public String getProviderName() {
        return properties.getWhatsapp().isRealMode() ? "HttpMetaWhatsAppProvider" : mockWhatsAppProvider.getProviderName();
    }

    @Override
    public NotificationProviderResult send(Notification notification) {
        if (!properties.getWhatsapp().isRealMode()) {
            return mockWhatsAppProvider.send(notification);
        }

        String phoneId = properties.getWhatsapp().getPhoneNumberId();
        String token = properties.getWhatsapp().getAccessToken();
        if (phoneId == null || phoneId.isBlank() || token == null || token.isBlank()) {
            log.warn("Meta WhatsApp phone_number_id or access_token missing, failing WhatsApp message permanently");
            return NotificationProviderResult.failure("Meta WhatsApp credentials missing", false);
        }

        String baseUrl = baseUrlOverride != null ? baseUrlOverride : "https://graph.facebook.com/v18.0";
        String endpoint = baseUrl + "/" + phoneId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", notification.getRecipient(),
                "type", "text",
                "text", Map.of("body", notification.getBody())
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(endpoint, HttpMethod.POST, request, Map.class);
            Map resBody = response.getBody();
            String messageId = "wmid." + UUID.randomUUID().toString().replace("-", "");
            if (resBody != null && resBody.containsKey("messages")) {
                List messages = (List) resBody.get("messages");
                if (messages != null && !messages.isEmpty() && messages.get(0) instanceof Map msgMap) {
                    if (msgMap.containsKey("id")) {
                        messageId = msgMap.get("id").toString();
                    }
                }
            }
            log.info("Meta WhatsApp message sent to '{}', wmid: {}", notification.getRecipient(), messageId);
            return NotificationProviderResult.success(messageId);
        } catch (HttpClientErrorException ex) {
            log.error("Meta WhatsApp HTTP 4xx error for recipient '{}': {}", notification.getRecipient(), ex.getMessage());
            boolean isTransient = ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS;
            return NotificationProviderResult.failure("Meta WhatsApp Client Error: " + ex.getStatusCode(), isTransient);
        } catch (HttpServerErrorException ex) {
            log.error("Meta WhatsApp HTTP 5xx error for recipient '{}': {}", notification.getRecipient(), ex.getMessage());
            return NotificationProviderResult.failure("Meta WhatsApp Server Error: " + ex.getStatusCode(), true);
        } catch (ResourceAccessException ex) {
            log.error("Meta WhatsApp timeout/network error for recipient '{}': {}", notification.getRecipient(), ex.getMessage());
            return NotificationProviderResult.failure("Meta WhatsApp Timeout: " + ex.getMessage(), true);
        } catch (Exception ex) {
            log.error("Meta WhatsApp unknown error for recipient '{}': {}", notification.getRecipient(), ex.getMessage());
            return NotificationProviderResult.failure("Meta WhatsApp Error: " + ex.getMessage(), true);
        }
    }
}
