package com.sporekart.modules.payment.infrastructure.provider;

import com.sporekart.modules.payment.domain.PaymentProviderType;

public interface PaymentProvider {

    PaymentProviderType getProviderType();

    PaymentProviderOrderResult createPaymentOrder(PaymentProviderOrderRequest request);

    boolean verifyPaymentSignature(PaymentVerificationRequest request);

    boolean verifyWebhookSignature(String rawBody, String signatureHeader);

    PaymentStatusResult fetchPaymentStatus(String providerPaymentId);
}
