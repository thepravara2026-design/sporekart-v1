package com.sporekart.modules.payment.infrastructure.persistence;

import com.sporekart.modules.payment.domain.PaymentProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataJpaWebhookEventRepository extends JpaRepository<PaymentWebhookEventEntity, UUID> {

    Optional<PaymentWebhookEventEntity> findByProviderAndProviderEventId(PaymentProviderType provider, String providerEventId);
}
