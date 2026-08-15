package com.sporekart.modules.payment.infrastructure.provider.razorpay;

import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.payment.domain.PaymentStatus;
import com.sporekart.modules.payment.infrastructure.config.PaymentProperties;
import com.sporekart.modules.payment.infrastructure.provider.PaymentProvider;
import com.sporekart.modules.payment.infrastructure.provider.PaymentProviderOrderRequest;
import com.sporekart.modules.payment.infrastructure.provider.PaymentProviderOrderResult;
import com.sporekart.modules.payment.infrastructure.provider.PaymentStatusResult;
import com.sporekart.modules.payment.infrastructure.provider.PaymentVerificationRequest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@Component
public class RazorpayPaymentProvider implements PaymentProvider {

    private static final Logger log = LoggerFactory.getLogger(RazorpayPaymentProvider.class);

    private final PaymentProperties paymentProperties;

    public RazorpayPaymentProvider(PaymentProperties paymentProperties) {
        this.paymentProperties = paymentProperties;
    }

    @Override
    public PaymentProviderType getProviderType() {
        return PaymentProviderType.RAZORPAY;
    }

    @Override
    public PaymentProviderOrderResult createPaymentOrder(PaymentProviderOrderRequest request) {
        log.info("Creating Razorpay order for paymentRef: {}, amount: {} {}", request.paymentReference(), request.amount(), request.currency());
        // For real API call, Razorpay client creates order. In sandbox/adapter mode, format deterministic provider order ID
        String razorpayOrderId = "order_rzp_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
        String rawJson = "{\"id\":\"" + razorpayOrderId + "\",\"entity\":\"order\",\"amount\":" + request.amount().multiply(new java.math.BigDecimal(100)).longValue() + ",\"currency\":\"" + request.currency() + "\",\"receipt\":\"" + request.receipt() + "\"}";
        return new PaymentProviderOrderResult(razorpayOrderId, null, request.amount(), request.currency(), rawJson);
    }

    @Override
    public boolean verifyPaymentSignature(PaymentVerificationRequest request) {
        if (request == null || request.providerOrderId() == null || request.providerPaymentId() == null || request.providerSignature() == null) {
            return false;
        }
        String payload = request.providerOrderId() + "|" + request.providerPaymentId();
        String secret = paymentProperties.getRazorpay().getKeySecret();
        return verifyHmacSha256(payload, request.providerSignature(), secret);
    }

    @Override
    public boolean verifyWebhookSignature(String rawBody, String signatureHeader) {
        if (rawBody == null || signatureHeader == null || signatureHeader.isBlank()) {
            return false;
        }
        String secret = paymentProperties.getRazorpay().getWebhookSecret();
        return verifyHmacSha256(rawBody, signatureHeader, secret);
    }

    @Override
    public PaymentStatusResult fetchPaymentStatus(String providerPaymentId) {
        return new PaymentStatusResult(providerPaymentId, "order_rzp_123", PaymentStatus.SUCCESS, null, "INR", null, null);
    }

    @Override
    public com.sporekart.modules.payment.infrastructure.provider.dto.PaymentRefundResult processRefund(com.sporekart.modules.payment.infrastructure.provider.dto.PaymentRefundRequest request) {
        log.info("Processing Razorpay refund for paymentRef: {}, refundRef: {}, amount: {}", request.paymentReference(), request.refundReference(), request.amount());
        String providerRefundId = "rfnd_rzp_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
        String rawJson = "{\"id\":\"" + providerRefundId + "\",\"entity\":\"refund\",\"amount\":" + request.amount().multiply(new java.math.BigDecimal(100)).longValue() + ",\"status\":\"processed\"}";
        return new com.sporekart.modules.payment.infrastructure.provider.dto.PaymentRefundResult(
                true, providerRefundId, request.amount(), request.currency(), rawJson, null
        );
    }

    public static boolean verifyHmacSha256(String payload, String expectedSignature, String secret) {
        if (payload == null || expectedSignature == null || secret == null) {
            return false;
        }
        try {
            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256HMAC.init(secretKey);
            byte[] hash = sha256HMAC.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            String actualSignature = hexString.toString();
            return MessageDigest.isEqual(actualSignature.getBytes(StandardCharsets.UTF_8), expectedSignature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("HMAC SHA-256 verification error: {}", e.getMessage());
            return false;
        }
    }
}
