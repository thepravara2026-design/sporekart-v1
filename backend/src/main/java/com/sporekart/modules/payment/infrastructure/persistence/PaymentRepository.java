package com.sporekart.modules.payment.infrastructure.persistence;

import com.sporekart.modules.payment.domain.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(UUID id);

    Optional<Payment> findByPaymentReference(String paymentReference);

    Optional<Payment> findByOrderId(UUID orderId);

    java.util.List<Payment> findAllByStatus(com.sporekart.modules.payment.domain.PaymentStatus status);
}
