package com.sporekart.modules.notification.infrastructure.provider;

import com.sporekart.modules.notification.domain.Notification;
import com.sporekart.modules.notification.domain.NotificationChannel;
import com.sporekart.modules.notification.infrastructure.config.NotificationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Component
public class HttpTwilioSmsProvider implements NotificationProvider {

    private static final Logger log = LoggerFactory.getLogger(HttpTwilioSmsProvider.class);

    private final NotificationProperties properties;
    private final MockSmsProvider mockSmsProvider;
    private final RestTemplate restTemplate;

    private String baseUrlOverride;

    public HttpTwilioSmsProvider(NotificationProperties properties,
                                 MockSmsProvider mockSmsProvider,
                                 @Autowired(required = false) RestTemplate restTemplate) {
        this.properties = properties;
        this.mockSmsProvider = mockSmsProvider;
        this.restTemplate = restTemplate != null ? restTemplate : new RestTemplate();
    }

    public void setBaseUrlOverride(String baseUrlOverride) {
        this.baseUrlOverride = baseUrlOverride;
    }

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.SMS;
    }

    @Override
    public String getProviderName() {
        return properties.getSms().isRealMode() ? "HttpTwilioSmsProvider" : mockSmsProvider.getProviderName();
    }

    @Override
    public NotificationProviderResult send(Notification notification) {
        if (!properties.getSms().isRealMode()) {
            return mockSmsProvider.send(notification);
        }

        String sid = properties.getSms().getAccountSid();
        String token = properties.getSms().getAuthToken();
        if (sid == null || sid.isBlank() || token == null || token.isBlank()) {
            log.warn("Twilio AccountSid or AuthToken missing, failing SMS permanently");
            return NotificationProviderResult.failure("Twilio credentials missing", false);
        }

        String baseUrl = baseUrlOverride != null ? baseUrlOverride : "https://api.twilio.com";
        String endpoint = baseUrl + "/2010-04-01/Accounts/" + sid + "/Messages.json";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(sid, token);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("To", notification.getRecipient());
        body.add("From", properties.getSms().getFromNumber() != null ? properties.getSms().getFromNumber() : "+15005550006");
        body.add("Body", notification.getBody());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(endpoint, HttpMethod.POST, request, Map.class);
            Map resBody = response.getBody();
            String messageSid = resBody != null && resBody.containsKey("sid") ? resBody.get("sid").toString() : "SM" + UUID.randomUUID().toString().replace("-", "");
            log.info("Twilio SMS sent to '{}', sid: {}", notification.getRecipient(), messageSid);
            return NotificationProviderResult.success(messageSid);
        } catch (HttpClientErrorException ex) {
            log.error("Twilio HTTP 4xx error for recipient '{}': {}", notification.getRecipient(), ex.getMessage());
            boolean isTransient = ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS;
            return NotificationProviderResult.failure("Twilio Client Error: " + ex.getStatusCode(), isTransient);
        } catch (HttpServerErrorException ex) {
            log.error("Twilio HTTP 5xx error for recipient '{}': {}", notification.getRecipient(), ex.getMessage());
            return NotificationProviderResult.failure("Twilio Server Error: " + ex.getStatusCode(), true);
        } catch (ResourceAccessException ex) {
            log.error("Twilio timeout/network error for recipient '{}': {}", notification.getRecipient(), ex.getMessage());
            return NotificationProviderResult.failure("Twilio Timeout: " + ex.getMessage(), true);
        } catch (Exception ex) {
            log.error("Twilio unknown error for recipient '{}': {}", notification.getRecipient(), ex.getMessage());
            return NotificationProviderResult.failure("Twilio Error: " + ex.getMessage(), true);
        }
    }
}
