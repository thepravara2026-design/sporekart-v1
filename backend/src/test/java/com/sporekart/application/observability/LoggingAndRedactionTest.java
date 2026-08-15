package com.sporekart.application.observability;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingAndRedactionTest {

    @Test
    @DisplayName("Should redact password, secret, token, and authorization headers in JSON logs")
    void shouldRedactSensitiveFieldsInJson() {
        String rawLog = "{\"user\":\"john@example.com\",\"password\":\"SecretPass123!\",\"token\":\"eyJhbGciOiJIUzI1NiJ9.test.sig\"}";
        String redacted = LogRedactor.redact(rawLog);

        assertThat(redacted).doesNotContain("SecretPass123!");
        assertThat(redacted).contains("\"password\":\"[REDACTED]\"");
        assertThat(redacted).contains("\"token\":\"[REDACTED]\"");
    }

    @Test
    @DisplayName("Should redact Bearer JWT tokens in Authorization headers")
    void shouldRedactBearerHeader() {
        String rawHeader = "Header: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.signature_hash_value";
        String redacted = LogRedactor.redact(rawHeader);

        assertThat(redacted).doesNotContain("signature_hash_value");
        assertThat(redacted).contains("Bearer [REDACTED]");
    }

    @Test
    @DisplayName("Should mask sensitive IDs showing only prefix and suffix")
    void shouldMaskSensitiveId() {
        String secretKey = "rzp_live_secret_key_999";
        String masked = LogRedactor.maskSensitiveId(secretKey);

        assertThat(masked).doesNotContain("secret_key");
        assertThat(masked).startsWith("rz");
        assertThat(masked).endsWith("99");
    }
}
