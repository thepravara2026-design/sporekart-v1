package com.sporekart.modules.payment.infrastructure.persistence;

import com.sporekart.modules.payment.domain.Payment;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class PaymentRepositoryImpl implements PaymentRepository {

    private final SpringDataJpaPaymentRepository jpaRepository;

    public PaymentRepositoryImpl(SpringDataJpaPaymentRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = PaymentEntity.fromDomain(payment);
        PaymentEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return jpaRepository.findByIdWithAttempts(id).map(PaymentEntity::toDomain);
    }

    @Override
    public Optional<Payment> findByPaymentReference(String paymentReference) {
        return jpaRepository.findByPaymentReferenceWithAttempts(paymentReference).map(PaymentEntity::toDomain);
    }

    @Override
    public Optional<Payment> findByOrderId(UUID orderId) {
        return jpaRepository.findByOrderIdWithAttempts(orderId).map(PaymentEntity::toDomain);
    }
}
