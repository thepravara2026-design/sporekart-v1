package com.sporekart.modules.payment;

import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.payment.infrastructure.config.PaymentProperties;
import com.sporekart.modules.payment.infrastructure.provider.PaymentProviderOrderRequest;
import com.sporekart.modules.payment.infrastructure.provider.PaymentProviderOrderResult;
import com.sporekart.modules.payment.infrastructure.provider.PaymentVerificationRequest;
import com.sporekart.modules.payment.infrastructure.provider.mock.MockPaymentProvider;
import com.sporekart.modules.payment.infrastructure.provider.razorpay.RazorpayPaymentProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentProviderTest {

    @Test
    @DisplayName("RazorpayPaymentProvider should correctly verify valid and tampered HMAC SHA-256 signatures")
    void testRazorpaySignatureVerification() {
        PaymentProperties properties = new PaymentProperties();
        properties.getRazorpay().setKeySecret("secret_key_123");
        properties.getRazorpay().setWebhookSecret("wh_secret_456");

        RazorpayPaymentProvider razorpay = new RazorpayPaymentProvider(properties);

        String orderId = "order_12345";
        String paymentId = "pay_67890";
        String payload = orderId + "|" + paymentId;

        // Calculate valid signature using the helper method
        String expectedSignature = RazorpayPaymentProvider.verifyHmacSha256(payload, "secret_key_123", "secret_key_123") ? "" : null;
        // Generate actual hex HMAC signature
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec("secret_key_123".getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            expectedSignature = hex.toString();
        } catch (Exception e) {
            // ignore
        }

        assertNotNull(expectedSignature);

        // Valid verification
        PaymentVerificationRequest validReq = new PaymentVerificationRequest(orderId, paymentId, expectedSignature);
        assertTrue(razorpay.verifyPaymentSignature(validReq));

        // Invalid signature
        PaymentVerificationRequest invalidReq = new PaymentVerificationRequest(orderId, paymentId, "invalid_sig_abc");
        assertFalse(razorpay.verifyPaymentSignature(invalidReq));

        // Tampered order ID
        PaymentVerificationRequest tamperedReq = new PaymentVerificationRequest("order_TAMPERED", paymentId, expectedSignature);
        assertFalse(razorpay.verifyPaymentSignature(tamperedReq));
    }

    @Test
    @DisplayName("MockPaymentProvider should produce deterministic mock orders and pass mock verification")
    void testMockPaymentProvider() {
        MockPaymentProvider mockProvider = new MockPaymentProvider();
        assertEquals(PaymentProviderType.MOCK, mockProvider.getProviderType());

        PaymentProviderOrderRequest request = new PaymentProviderOrderRequest("PAY-001", "ATT-001", UUID.randomUUID(), new BigDecimal("999.00"), "INR", "PAY-001");
        PaymentProviderOrderResult result = mockProvider.createPaymentOrder(request);

        assertNotNull(result);
        assertNotNull(result.providerOrderId());
        assertTrue(result.providerOrderId().startsWith("order_mock_"));

        assertTrue(mockProvider.verifyPaymentSignature(new PaymentVerificationRequest("order_mock_1", "pay_mock_1", "valid_sig")));
        assertFalse(mockProvider.verifyPaymentSignature(new PaymentVerificationRequest("order_mock_1", "pay_mock_1", "INVALID_SIGNATURE")));
    }
}
