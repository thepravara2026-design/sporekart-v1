package com.sporekart.modules.payment.infrastructure.provider.mock;

import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.payment.domain.PaymentStatus;
import com.sporekart.modules.payment.infrastructure.provider.PaymentProvider;
import com.sporekart.modules.payment.infrastructure.provider.PaymentProviderOrderRequest;
import com.sporekart.modules.payment.infrastructure.provider.PaymentProviderOrderResult;
import com.sporekart.modules.payment.infrastructure.provider.PaymentStatusResult;
import com.sporekart.modules.payment.infrastructure.provider.PaymentVerificationRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockPaymentProvider implements PaymentProvider {

    @Override
    public PaymentProviderType getProviderType() {
        return PaymentProviderType.MOCK;
    }

    @Override
    public PaymentProviderOrderResult createPaymentOrder(PaymentProviderOrderRequest request) {
        String mockProviderOrderId = "order_mock_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
        String mockRawResponse = "{\"id\":\"" + mockProviderOrderId + "\",\"entity\":\"order\",\"amount\":" + request.amount() + ",\"status\":\"created\"}";
        return new PaymentProviderOrderResult(mockProviderOrderId, null, request.amount(), request.currency(), mockRawResponse);
    }

    @Override
    public boolean verifyPaymentSignature(PaymentVerificationRequest request) {
        if (request == null || request.providerOrderId() == null || request.providerPaymentId() == null || request.providerSignature() == null) {
            return false;
        }
        if (request.providerSignature().equals("INVALID_SIGNATURE")) {
            return false;
        }
        return true;
    }

    @Override
    public boolean verifyWebhookSignature(String rawBody, String signatureHeader) {
        if (signatureHeader == null || signatureHeader.isBlank() || signatureHeader.equals("INVALID_SIGNATURE")) {
            return false;
        }
        return true;
    }

    @Override
    public PaymentStatusResult fetchPaymentStatus(String providerPaymentId) {
        return new PaymentStatusResult(providerPaymentId, "order_mock_123", PaymentStatus.SUCCESS, null, "INR", null, null);
    }
}
