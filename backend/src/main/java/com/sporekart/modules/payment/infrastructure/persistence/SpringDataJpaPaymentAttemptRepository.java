package com.sporekart.modules.payment.infrastructure.persistence;

import com.sporekart.modules.payment.domain.PaymentProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataJpaPaymentAttemptRepository extends JpaRepository<PaymentAttemptEntity, UUID> {

    Optional<PaymentAttemptEntity> findByAttemptReference(String attemptReference);

    Optional<PaymentAttemptEntity> findByProviderAndProviderOrderId(PaymentProviderType provider, String providerOrderId);

    Optional<PaymentAttemptEntity> findByProviderAndProviderPaymentId(PaymentProviderType provider, String providerPaymentId);
}
