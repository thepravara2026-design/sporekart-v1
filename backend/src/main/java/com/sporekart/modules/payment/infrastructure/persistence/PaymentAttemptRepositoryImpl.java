package com.sporekart.modules.payment.infrastructure.persistence;

import com.sporekart.modules.payment.domain.PaymentAttempt;
import com.sporekart.modules.payment.domain.PaymentProviderType;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class PaymentAttemptRepositoryImpl implements PaymentAttemptRepository {

    private final SpringDataJpaPaymentAttemptRepository jpaRepository;

    public PaymentAttemptRepositoryImpl(SpringDataJpaPaymentAttemptRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PaymentAttempt save(PaymentAttempt attempt) {
        PaymentAttemptEntity entity = PaymentAttemptEntity.fromDomain(attempt, null);
        PaymentAttemptEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<PaymentAttempt> findById(UUID id) {
        return jpaRepository.findById(id).map(PaymentAttemptEntity::toDomain);
    }

    @Override
    public Optional<PaymentAttempt> findByAttemptReference(String attemptReference) {
        return jpaRepository.findByAttemptReference(attemptReference).map(PaymentAttemptEntity::toDomain);
    }

    @Override
    public Optional<PaymentAttempt> findByProviderAndProviderOrderId(PaymentProviderType provider, String providerOrderId) {
        return jpaRepository.findByProviderAndProviderOrderId(provider, providerOrderId).map(PaymentAttemptEntity::toDomain);
    }

    @Override
    public Optional<PaymentAttempt> findByProviderAndProviderPaymentId(PaymentProviderType provider, String providerPaymentId) {
        return jpaRepository.findByProviderAndProviderPaymentId(provider, providerPaymentId).map(PaymentAttemptEntity::toDomain);
    }
}
