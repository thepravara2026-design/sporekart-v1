package com.sporekart.modules.payment.infrastructure.persistence;

import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.payment.domain.PaymentWebhookEvent;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class WebhookEventRepositoryImpl implements WebhookEventRepository {

    private final SpringDataJpaWebhookEventRepository jpaRepository;

    public WebhookEventRepositoryImpl(SpringDataJpaWebhookEventRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PaymentWebhookEvent save(PaymentWebhookEvent event) {
        PaymentWebhookEventEntity entity = PaymentWebhookEventEntity.fromDomain(event);
        PaymentWebhookEventEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<PaymentWebhookEvent> findByProviderAndProviderEventId(PaymentProviderType provider, String providerEventId) {
        return jpaRepository.findByProviderAndProviderEventId(provider, providerEventId).map(PaymentWebhookEventEntity::toDomain);
    }
}
