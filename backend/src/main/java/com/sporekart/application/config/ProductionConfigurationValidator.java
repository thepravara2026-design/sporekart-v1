package com.sporekart.application.config;

import com.sporekart.modules.notification.infrastructure.config.NotificationProperties;
import com.sporekart.modules.payment.infrastructure.config.PaymentProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Startup fail-fast configuration validator for PRODUCTION and STAGING profiles.
 *
 * <p>Ensures that critical production secrets (JWT secret, DB password, Razorpay secret, Webhook secret)
 * are explicitly externalized via environment variables and do not silently fall back
 * to insecure development placeholders or mock providers.
 */
@Component
@Profile({"prod", "staging"})
public class ProductionConfigurationValidator {

    private static final Logger log = LoggerFactory.getLogger(ProductionConfigurationValidator.class);

    private static final String PLACEHOLDER_JWT_SECRET = "CHANGE_ME_MIN_64_CHAR_RANDOM_SECRET_KEY_FOR_HMAC_SHA512_SIGNING_SPEC";
    private static final String PLACEHOLDER_DB_PASSWORD = "CHANGE_ME_PRODUCTION_STRONG_PASSWORD";
    private static final String PLACEHOLDER_PAYMENT_KEY_SECRET = "mockSecretKey456";
    private static final String PLACEHOLDER_PAYMENT_WEBHOOK_SECRET = "mockWebhookSecret789";
    private static final String PLACEHOLDER_NOTIFICATION_WEBHOOK_SECRET = "sporekart-default-webhook-secret";

    @Value("${app.security.jwt.secret:}")
    private String jwtSecret;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    @Value("${spring.datasource.username:}")
    private String dbUsername;

    @Value("${sporekart.payment.razorpay.key-secret:}")
    private String paymentSecret;

    @Value("${sporekart.payment.razorpay.webhook-secret:}")
    private String paymentWebhookSecret;

    @Value("${sporekart.payment.provider:MOCK}")
    private String paymentProvider;

    @Value("${sporekart.shipping.provider:MOCK}")
    private String shippingProvider;

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String allowedOrigins;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    private final NotificationProperties notificationProperties;
    private final PaymentProperties paymentProperties;

    public ProductionConfigurationValidator(
            @Autowired(required = false) NotificationProperties notificationProperties,
            @Autowired(required = false) PaymentProperties paymentProperties
    ) {
        this.notificationProperties = notificationProperties;
        this.paymentProperties = paymentProperties;
    }

    @PostConstruct
    public void validateProductionConfiguration() {
        log.info("Executing ProductionConfigurationValidator check for active profile: {}", activeProfile);

        // 1. JWT Secret Validation
        if (jwtSecret == null || jwtSecret.isBlank() || PLACEHOLDER_JWT_SECRET.equals(jwtSecret)) {
            throw new IllegalStateException(
                "CRITICAL PRODUCTION CONFIGURATION ERROR: Active profile is '" + activeProfile +
                "' but app.security.jwt.secret is missing, empty, or using default development fallback. " +
                "Set JWT_SECRET_KEY in environment variables."
            );
        }

        if (jwtSecret.length() < 32) {
            throw new IllegalStateException(
                "CRITICAL PRODUCTION CONFIGURATION ERROR: JWT secret must be at least 32 characters (256 bits). Current length: " + jwtSecret.length()
            );
        }

        // 2. Database Password & Username Validation
        if (dbPassword == null || dbPassword.isBlank() || PLACEHOLDER_DB_PASSWORD.equals(dbPassword)) {
            throw new IllegalStateException(
                "CRITICAL PRODUCTION CONFIGURATION ERROR: Active profile is '" + activeProfile +
                "' but spring.datasource.password is missing, empty, or using placeholder. " +
                "Set DATABASE_PASSWORD in environment variables."
            );
        }

        if (dbUsername == null || dbUsername.isBlank()) {
            throw new IllegalStateException(
                "CRITICAL PRODUCTION CONFIGURATION ERROR: Active profile is '" + activeProfile +
                "' but spring.datasource.username is missing or empty. Set DATABASE_USERNAME in environment variables."
            );
        }

        // 3. Payment Secret & Webhook Secret Validation
        String effectivePaymentSecret = (paymentSecret != null && !paymentSecret.isBlank()) ? paymentSecret : (paymentProperties != null && paymentProperties.getRazorpay() != null ? paymentProperties.getRazorpay().getKeySecret() : null);
        if (effectivePaymentSecret == null || effectivePaymentSecret.isBlank() || PLACEHOLDER_PAYMENT_KEY_SECRET.equals(effectivePaymentSecret)) {
            throw new IllegalStateException(
                "CRITICAL PRODUCTION CONFIGURATION ERROR: Active profile is '" + activeProfile +
                "' but sporekart.payment.razorpay.key-secret is missing, empty, or using default mock placeholder. " +
                "Set PAYMENT_PROVIDER_SECRET in environment variables."
            );
        }

        String effectivePaymentWebhookSecret = (paymentWebhookSecret != null && !paymentWebhookSecret.isBlank()) ? paymentWebhookSecret : (paymentProperties != null && paymentProperties.getRazorpay() != null ? paymentProperties.getRazorpay().getWebhookSecret() : null);
        if (effectivePaymentWebhookSecret == null || effectivePaymentWebhookSecret.isBlank() || PLACEHOLDER_PAYMENT_WEBHOOK_SECRET.equals(effectivePaymentWebhookSecret)) {
            throw new IllegalStateException(
                "CRITICAL PRODUCTION CONFIGURATION ERROR: Active profile is '" + activeProfile +
                "' but sporekart.payment.razorpay.webhook-secret is missing, empty, or using default mock placeholder. " +
                "Set PAYMENT_WEBHOOK_SECRET in environment variables."
            );
        }

        // 4. Notification Real Channel Webhook Secret Validation
        if (notificationProperties != null) {
            List<NotificationProperties.ChannelProperties> realChannels = List.of(
                notificationProperties.getEmail(),
                notificationProperties.getSms(),
                notificationProperties.getWhatsapp(),
                notificationProperties.getPush()
            );

            for (var channel : realChannels) {
                if (channel != null && channel.isRealMode()) {
                    if (PLACEHOLDER_NOTIFICATION_WEBHOOK_SECRET.equals(channel.getWebhookSecret())) {
                        throw new IllegalStateException(
                            "CRITICAL PRODUCTION CONFIGURATION ERROR: Active profile is '" + activeProfile +
                            "' and a notification channel is configured in real mode, but webhookSecret uses the default placeholder '" +
                            PLACEHOLDER_NOTIFICATION_WEBHOOK_SECRET + "'."
                        );
                    }
                }
            }
        }

        // 5. CORS Credentials + Wildcard Validation
        if (allowedOrigins != null && (allowedOrigins.contains("*") || allowedOrigins.trim().equals("*"))) {
            throw new IllegalStateException(
                "CRITICAL PRODUCTION CONFIGURATION ERROR: Active profile is '" + activeProfile +
                "' but app.cors.allowed-origins includes wildcard '*' while credentials are enabled."
            );
        }

        // 6. Strict Production Provider Isolation: Fail startup if provider is unconfigured or MOCK in prod
        if ("prod".equalsIgnoreCase(activeProfile)) {
            if (paymentProvider == null || paymentProvider.isBlank() || "MOCK".equalsIgnoreCase(paymentProvider)) {
                throw new IllegalStateException(
                    "CRITICAL PRODUCTION CONFIGURATION ERROR: Active profile is 'prod' but sporekart.payment.provider is unconfigured or set to MOCK. " +
                    "Set PAYMENT_PROVIDER environment variable (e.g. RAZORPAY)."
                );
            }
            if (shippingProvider == null || shippingProvider.isBlank() || "MOCK".equalsIgnoreCase(shippingProvider)) {
                throw new IllegalStateException(
                    "CRITICAL PRODUCTION CONFIGURATION ERROR: Active profile is 'prod' but sporekart.shipping.provider is unconfigured or set to MOCK. " +
                    "Set SHIPPING_PROVIDER environment variable (e.g. SHIPROCKET)."
                );
            }
        }

        log.info("ProductionConfigurationValidator PASSED: All critical environment variables and secrets are validated for profile '{}'.", activeProfile);
    }
}
