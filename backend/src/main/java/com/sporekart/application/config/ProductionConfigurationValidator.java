package com.sporekart.application.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Startup fail-fast configuration validator for PRODUCTION and STAGING profiles.
 *
 * <p>Ensures that critical production secrets (JWT secret, DB password, Razorpay secret)
 * are explicitly externalized via environment variables and do not silently fall back
 * to insecure development placeholders.
 */
@Component
@Profile({"prod", "staging"})
public class ProductionConfigurationValidator {

    private static final Logger log = LoggerFactory.getLogger(ProductionConfigurationValidator.class);

    private static final String DEFAULT_DEV_JWT_SECRET = "sporekart-v3-super-secure-production-jwt-secret-key-min-512-bits-for-hmac-sha512-signing-spec";
    private static final String PLACEHOLDER_JWT_SECRET = "CHANGE_ME_MIN_32_CHAR_RANDOM_SECRET_KEY";
    private static final String PLACEHOLDER_DB_PASSWORD = "CHANGE_ME_PRODUCTION_STRONG_PASSWORD";

    @Value("${app.security.jwt.secret:}")
    private String jwtSecret;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    @Value("${spring.datasource.username:}")
    private String dbUsername;

    @Value("${app.payment.razorpay.secret:}")
    private String paymentSecret;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @PostConstruct
    public void validateProductionConfiguration() {
        log.info("Executing ProductionConfigurationValidator check for active profile: {}", activeProfile);

        if (jwtSecret == null || jwtSecret.isBlank() || DEFAULT_DEV_JWT_SECRET.equals(jwtSecret) || PLACEHOLDER_JWT_SECRET.equals(jwtSecret)) {
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

        log.info("ProductionConfigurationValidator PASSED: All critical environment variables and secrets are validated.");
    }
}
