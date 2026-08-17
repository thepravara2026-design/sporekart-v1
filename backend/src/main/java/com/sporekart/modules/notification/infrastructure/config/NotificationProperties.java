package com.sporekart.modules.notification.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.notification")
public class NotificationProperties {

    private ChannelProperties email = new ChannelProperties("mock", "sendgrid");
    private ChannelProperties sms = new ChannelProperties("mock", "twilio");
    private ChannelProperties whatsapp = new ChannelProperties("mock", "meta");
    private ChannelProperties push = new ChannelProperties("mock", "mock");
    private ResilienceProperties resilience = new ResilienceProperties();

    public ChannelProperties getEmail() { return email; }
    public void setEmail(ChannelProperties email) { this.email = email; }

    public ChannelProperties getSms() { return sms; }
    public void setSms(ChannelProperties sms) { this.sms = sms; }

    public ChannelProperties getWhatsapp() { return whatsapp; }
    public void setWhatsapp(ChannelProperties whatsapp) { this.whatsapp = whatsapp; }

    public ChannelProperties getPush() { return push; }
    public void setPush(ChannelProperties push) { this.push = push; }

    public ResilienceProperties getResilience() { return resilience; }
    public void setResilience(ResilienceProperties resilience) { this.resilience = resilience; }

    public static class ChannelProperties {
        private String mode = "mock"; // mock | real
        private String provider = "mock";
        private String apiKey;
        private String accountSid;
        private String authToken;
        private String fromNumber;
        private String fromEmail = "notifications@sporekart.com";
        private String fromName = "Sporekart";
        private String phoneNumberId;
        private String accessToken;
        private String webhookSecret = "sporekart-default-webhook-secret";
        private int connectTimeoutMs = 5000;
        private int readTimeoutMs = 5000;

        public ChannelProperties() {}

        public ChannelProperties(String mode, String provider) {
            this.mode = mode;
            this.provider = provider;
        }

        public boolean isRealMode() {
            return "real".equalsIgnoreCase(mode);
        }

        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }

        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }

        public String getAccountSid() { return accountSid; }
        public void setAccountSid(String accountSid) { this.accountSid = accountSid; }

        public String getAuthToken() { return authToken; }
        public void setAuthToken(String authToken) { this.authToken = authToken; }

        public String getFromNumber() { return fromNumber; }
        public void setFromNumber(String fromNumber) { this.fromNumber = fromNumber; }

        public String getFromEmail() { return fromEmail; }
        public void setFromEmail(String fromEmail) { this.fromEmail = fromEmail; }

        public String getFromName() { return fromName; }
        public void setFromName(String fromName) { this.fromName = fromName; }

        public String getPhoneNumberId() { return phoneNumberId; }
        public void setPhoneNumberId(String phoneNumberId) { this.phoneNumberId = phoneNumberId; }

        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

        public String getWebhookSecret() { return webhookSecret; }
        public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }

        public int getConnectTimeoutMs() { return connectTimeoutMs; }
        public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }

        public int getReadTimeoutMs() { return readTimeoutMs; }
        public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
    }

    public static class ResilienceProperties {
        private boolean circuitBreakerEnabled = true;
        private int failureThreshold = 3;
        private long openDurationMs = 10000;
        private int halfOpenProbes = 1;
        private long baseRetryDelayMs = 2000;
        private long maxRetryDelayMs = 300000;
        private long jitterMaxMs = 1000;
        private boolean staleProcessingEnabled = true;
        private long staleProcessingThresholdSeconds = 300;
        private boolean reconciliationEnabled = true;
        private long reconciliationStaleSentSeconds = 600;

        public boolean isCircuitBreakerEnabled() { return circuitBreakerEnabled; }
        public void setCircuitBreakerEnabled(boolean circuitBreakerEnabled) { this.circuitBreakerEnabled = circuitBreakerEnabled; }

        public int getFailureThreshold() { return failureThreshold; }
        public void setFailureThreshold(int failureThreshold) { this.failureThreshold = failureThreshold; }

        public long getOpenDurationMs() { return openDurationMs; }
        public void setOpenDurationMs(long openDurationMs) { this.openDurationMs = openDurationMs; }

        public int getHalfOpenProbes() { return halfOpenProbes; }
        public void setHalfOpenProbes(int halfOpenProbes) { this.halfOpenProbes = halfOpenProbes; }

        public long getBaseRetryDelayMs() { return baseRetryDelayMs; }
        public void setBaseRetryDelayMs(long baseRetryDelayMs) { this.baseRetryDelayMs = baseRetryDelayMs; }

        public long getMaxRetryDelayMs() { return maxRetryDelayMs; }
        public void setMaxRetryDelayMs(long maxRetryDelayMs) { this.maxRetryDelayMs = maxRetryDelayMs; }

        public long getJitterMaxMs() { return jitterMaxMs; }
        public void setJitterMaxMs(long jitterMaxMs) { this.jitterMaxMs = jitterMaxMs; }

        public boolean isStaleProcessingEnabled() { return staleProcessingEnabled; }
        public void setStaleProcessingEnabled(boolean staleProcessingEnabled) { this.staleProcessingEnabled = staleProcessingEnabled; }

        public long getStaleProcessingThresholdSeconds() { return staleProcessingThresholdSeconds; }
        public void setStaleProcessingThresholdSeconds(long staleProcessingThresholdSeconds) { this.staleProcessingThresholdSeconds = staleProcessingThresholdSeconds; }

        public boolean isReconciliationEnabled() { return reconciliationEnabled; }
        public void setReconciliationEnabled(boolean reconciliationEnabled) { this.reconciliationEnabled = reconciliationEnabled; }

        public long getReconciliationStaleSentSeconds() { return reconciliationStaleSentSeconds; }
        public void setReconciliationStaleSentSeconds(long reconciliationStaleSentSeconds) { this.reconciliationStaleSentSeconds = reconciliationStaleSentSeconds; }
    }
}
