package com.sporekart.modules.payment.infrastructure.config;

import com.sporekart.modules.payment.domain.PaymentProviderType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sporekart.payment")
public class PaymentProperties {

    private PaymentProviderType provider = PaymentProviderType.MOCK;
    private Razorpay razorpay = new Razorpay();

    public PaymentProviderType getProvider() {
        return provider;
    }

    public void setProvider(PaymentProviderType provider) {
        this.provider = provider;
    }

    public Razorpay getRazorpay() {
        return razorpay;
    }

    public void setRazorpay(Razorpay razorpay) {
        this.razorpay = razorpay;
    }

    public static class Razorpay {
        private String keyId = "rzp_test_mockKeyId123";
        private String keySecret = "mockSecretKey456";
        private String webhookSecret = "mockWebhookSecret789";

        public String getKeyId() {
            return keyId;
        }

        public void setKeyId(String keyId) {
            this.keyId = keyId;
        }

        public String getKeySecret() {
            return keySecret;
        }

        public void setKeySecret(String keySecret) {
            this.keySecret = keySecret;
        }

        public String getWebhookSecret() {
            return webhookSecret;
        }

        public void setWebhookSecret(String webhookSecret) {
            this.webhookSecret = webhookSecret;
        }
    }
}
