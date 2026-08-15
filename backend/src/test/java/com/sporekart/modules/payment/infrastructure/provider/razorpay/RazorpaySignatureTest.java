package com.sporekart.modules.payment.infrastructure.provider.razorpay;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RazorpaySignatureTest {

    private static final String SECRET = "test_razorpay_secret_key_12345";

    @Test
    @DisplayName("Valid HMAC SHA-256 Signature Verification Succeeds")
    void testValidSignatureVerification() {
        String payload = "order_rzp_123456|pay_rzp_789012";
        // Calculate expected HMAC SHA-256 using helper
        String expectedSignature = calculateHmacSha256(payload, SECRET);

        boolean result = RazorpayPaymentProvider.verifyHmacSha256(payload, expectedSignature, SECRET);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Tampered Signature Or Payload Verification Fails")
    void testTamperedSignatureFails() {
        String payload = "order_rzp_123456|pay_rzp_789012";
        String validSignature = calculateHmacSha256(payload, SECRET);

        // Tampered payload
        boolean tamperedPayloadResult = RazorpayPaymentProvider.verifyHmacSha256("order_rzp_123456|pay_rzp_999999", validSignature, SECRET);
        assertThat(tamperedPayloadResult).isFalse();

        // Invalid secret
        boolean invalidSecretResult = RazorpayPaymentProvider.verifyHmacSha256(payload, validSignature, "wrong_secret");
        assertThat(invalidSecretResult).isFalse();
    }

    private String calculateHmacSha256(String payload, String secret) {
        try {
            javax.crypto.Mac sha256HMAC = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
            sha256HMAC.init(secretKey);
            byte[] hash = sha256HMAC.doFinal(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
