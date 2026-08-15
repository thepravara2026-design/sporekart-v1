package com.sporekart.application.observability;

import java.util.regex.Pattern;

/**
 * Utility for redacting sensitive fields (passwords, JWT tokens, authorization headers,
 * payment secrets, CVV, card numbers) from log statements and diagnostic output.
 */
public final class LogRedactor {

    private LogRedactor() {}

    private static final Pattern SENSITIVE_KEY_PATTERN = Pattern.compile(
            "(?i)\"(password|secret|token|authorization|refreshToken|jwt|cvv|cardNumber|accountNumber|razorpay_secret|webhookSecret)\"\\s*:\\s*\"[^\"]+\""
    );

    private static final Pattern BEARER_PATTERN = Pattern.compile("(?i)Bearer\\s+[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+");

    public static String redact(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        String sanitized = SENSITIVE_KEY_PATTERN.matcher(input).replaceAll("\"$1\":\"[REDACTED]\"");
        sanitized = BEARER_PATTERN.matcher(sanitized).replaceAll("Bearer [REDACTED]");
        return sanitized;
    }

    public static String maskSensitiveId(String id) {
        if (id == null || id.length() <= 4) {
            return "****";
        }
        return id.substring(0, 2) + "****" + id.substring(id.length() - 2);
    }
}
