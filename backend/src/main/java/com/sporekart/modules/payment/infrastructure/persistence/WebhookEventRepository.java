package com.sporekart.modules.payment.infrastructure.persistence;

import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.payment.domain.PaymentWebhookEvent;

import java.util.Optional;

public interface WebhookEventRepository {

    PaymentWebhookEvent save(PaymentWebhookEvent event);

    Optional<PaymentWebhookEvent> findByProviderAndProviderEventId(PaymentProviderType provider, String providerEventId);
}
