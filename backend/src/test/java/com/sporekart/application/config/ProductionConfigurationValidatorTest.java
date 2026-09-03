package com.sporekart.application.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductionConfigurationValidatorTest {

    private ProductionConfigurationValidator createValidValidator(String profile) {
        ProductionConfigurationValidator validator = new ProductionConfigurationValidator(null, null);
        ReflectionTestUtils.setField(validator, "jwtSecret", "a-very-long-and-secure-jwt-secret-key-that-is-at-least-32-chars");
        ReflectionTestUtils.setField(validator, "dbPassword", "strong_prod_db_pass_123");
        ReflectionTestUtils.setField(validator, "dbUsername", "sporekart_user");
        ReflectionTestUtils.setField(validator, "paymentSecret", "real_razorpay_secret_999");
        ReflectionTestUtils.setField(validator, "paymentWebhookSecret", "real_webhook_secret_888");
        ReflectionTestUtils.setField(validator, "paymentProvider", "RAZORPAY");
        ReflectionTestUtils.setField(validator, "shippingProvider", "SHIPROCKET");
        ReflectionTestUtils.setField(validator, "allowedOrigins", "http://localhost:5173");
        ReflectionTestUtils.setField(validator, "activeProfile", profile);
        return validator;
    }

    @Test
    @DisplayName("Validator throws IllegalStateException when payment secret is placeholder in prod")
    void throwsWhenPaymentSecretIsPlaceholderInProd() {
        ProductionConfigurationValidator validator = createValidValidator("prod");
        ReflectionTestUtils.setField(validator, "paymentSecret", "mockSecretKey456");

        assertThatThrownBy(validator::validateProductionConfiguration)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sporekart.payment.razorpay.key-secret");
    }

    @Test
    @DisplayName("Validator throws IllegalStateException when payment webhook secret is placeholder in staging")
    void throwsWhenPaymentWebhookSecretIsPlaceholderInStaging() {
        ProductionConfigurationValidator validator = createValidValidator("staging");
        ReflectionTestUtils.setField(validator, "paymentWebhookSecret", "mockWebhookSecret789");

        assertThatThrownBy(validator::validateProductionConfiguration)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sporekart.payment.razorpay.webhook-secret");
    }

    @Test
    @DisplayName("Validator passes when all production secrets are non-placeholder values")
    void passesWhenNonPlaceholderSecretsSupplied() {
        ProductionConfigurationValidator validator = createValidValidator("prod");
        assertThatCode(validator::validateProductionConfiguration).doesNotThrowAnyException();
    }
}
