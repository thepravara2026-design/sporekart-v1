package com.sporekart.modules.returns;

import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.returns.domain.Return;
import com.sporekart.modules.returns.domain.ReturnReasonCode;
import com.sporekart.modules.returns.domain.ReturnRepository;
import com.sporekart.modules.returns.infrastructure.persistence.RefundRecordEntity;
import com.sporekart.modules.returns.infrastructure.persistence.SpringDataJpaRefundRecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentRefundIdempotencyTest {

    @Autowired
    private SpringDataJpaRefundRecordRepository refundRecordRepository;

    @Autowired
    private ReturnRepository returnRepository;

    @Test
    @DisplayName("Duplicate refund record with same idempotency key should be rejected by DB unique constraint")
    void testRefundIdempotencyConstraint() {
        Return returnAgg = Return.createNewRequest(
                "RET-IDEM-" + UUID.randomUUID().toString().substring(0, 6),
                UUID.randomUUID(),
                "ORD-IDEM-1",
                "cust-101",
                ReturnReasonCode.DAMAGED,
                "Damaged",
                null,
                "v1.0"
        );
        returnAgg = returnRepository.save(returnAgg);

        String key = "RFD-IDEMPOTENT-KEY-" + UUID.randomUUID();
        RefundRecordEntity entity1 = new RefundRecordEntity(
                UUID.randomUUID(), "RFD-REF-" + UUID.randomUUID().toString().substring(0, 6), returnAgg.getId(), returnAgg.getReturnReference(), returnAgg.getOrderId(), "cust-101",
                UUID.randomUUID(), "PAY-101", PaymentProviderType.MOCK, new BigDecimal("200.00"), "INR", "PROCESSED",
                null, "rfnd_mock_1", key, 0L, OffsetDateTime.now(), OffsetDateTime.now()
        );
        refundRecordRepository.saveAndFlush(entity1);

        Optional<RefundRecordEntity> found = refundRecordRepository.findByIdempotencyKey(key);
        assertTrue(found.isPresent());

        RefundRecordEntity entity2 = new RefundRecordEntity(
                UUID.randomUUID(), "RFD-REF-" + UUID.randomUUID().toString().substring(0, 6), returnAgg.getId(), returnAgg.getReturnReference(), returnAgg.getOrderId(), "cust-101",
                UUID.randomUUID(), "PAY-101", PaymentProviderType.MOCK, new BigDecimal("200.00"), "INR", "PROCESSED",
                null, "rfnd_mock_2", key, 0L, OffsetDateTime.now(), OffsetDateTime.now()
        );

        assertThrows(Exception.class, () -> refundRecordRepository.saveAndFlush(entity2));
    }
}
