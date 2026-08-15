package com.sporekart.modules.payment.infrastructure.persistence;

import com.sporekart.modules.payment.domain.PaymentAttempt;
import com.sporekart.modules.payment.domain.PaymentProviderType;

import java.util.Optional;
import java.util.UUID;

public interface PaymentAttemptRepository {

    PaymentAttempt save(PaymentAttempt attempt);

    Optional<PaymentAttempt> findById(UUID id);

    Optional<PaymentAttempt> findByAttemptReference(String attemptReference);

    Optional<PaymentAttempt> findByProviderAndProviderOrderId(PaymentProviderType provider, String providerOrderId);

    Optional<PaymentAttempt> findByProviderAndProviderPaymentId(PaymentProviderType provider, String providerPaymentId);
}
