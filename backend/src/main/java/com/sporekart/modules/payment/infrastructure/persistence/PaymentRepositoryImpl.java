package com.sporekart.modules.payment.infrastructure.persistence;

import com.sporekart.modules.payment.domain.Payment;
import org.springframework.stereotype.Repository;

import java.util.List;
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
        Optional<PaymentEntity> existingOpt = jpaRepository.findById(payment.getId());
        PaymentEntity entity;
        if (existingOpt.isPresent()) {
            entity = existingOpt.get();
            entity.setStatus(payment.getStatus());
            entity.setActiveAttemptId(payment.getActiveAttemptId());
            entity.setAmount(payment.getAmount());
            entity.setCurrency(payment.getCurrency());
            entity.setUpdatedAt(payment.getUpdatedAt());
            if (payment.getAttempts() != null) {
                List<PaymentAttemptEntity> attemptEntities = payment.getAttempts().stream()
                        .map(att -> PaymentAttemptEntity.fromDomain(att, entity))
                        .toList();
                entity.setAttempts(attemptEntities);
            }
        } else {
            entity = PaymentEntity.fromDomain(payment);
        }
        PaymentEntity saved = jpaRepository.saveAndFlush(entity);
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

    @Override
    public List<Payment> findAllByStatus(com.sporekart.modules.payment.domain.PaymentStatus status) {
        return jpaRepository.findAllByStatus(status).stream().map(PaymentEntity::toDomain).toList();
    }
}
