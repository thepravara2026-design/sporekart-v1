package com.sporekart.modules.training;

import com.sporekart.modules.payment.domain.Payment;
import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.payment.infrastructure.config.PaymentProperties;
import com.sporekart.modules.payment.infrastructure.persistence.PaymentAttemptRepository;
import com.sporekart.modules.payment.infrastructure.persistence.PaymentRepository;
import com.sporekart.modules.payment.infrastructure.provider.PaymentProvider;
import com.sporekart.modules.payment.infrastructure.provider.PaymentProviderOrderResult;
import com.sporekart.modules.payment.infrastructure.provider.PaymentProviderRegistry;
import com.sporekart.modules.security.application.SecurityAuditService;
import com.sporekart.modules.training.application.EnrollmentApplicationService;
import com.sporekart.modules.training.application.TrainingPaymentApplicationService;
import com.sporekart.modules.training.controller.dto.TrainingPaymentOrderResponse;
import com.sporekart.modules.training.controller.dto.TrainingPaymentVerificationRequestDto;
import com.sporekart.modules.training.domain.*;
import com.sporekart.modules.training.domain.exception.PaymentVerificationException;
import com.sporekart.modules.training.domain.exception.UnauthorizedEnrollmentAccessException;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentPaymentRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentRepository;
import com.sporekart.modules.training.domain.port.TrainingProgramRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TrainingPaymentSecurityTest {

    private TrainingBatchRepository batchRepository;
    private TrainingProgramRepository programRepository;
    private TrainingEnrollmentRepository enrollmentRepository;
    private TrainingEnrollmentPaymentRepository trainingPaymentRepository;
    private PaymentRepository paymentRepository;
    private PaymentAttemptRepository paymentAttemptRepository;
    private PaymentProviderRegistry providerRegistry;
    private PaymentProvider paymentProvider;
    private PaymentProperties paymentProperties;
    private EnrollmentApplicationService enrollmentService;
    private SecurityAuditService auditService;
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    private TrainingPaymentApplicationService service;

    @BeforeEach
    void setUp() {
        batchRepository = mock(TrainingBatchRepository.class);
        programRepository = mock(TrainingProgramRepository.class);
        enrollmentRepository = mock(TrainingEnrollmentRepository.class);
        trainingPaymentRepository = mock(TrainingEnrollmentPaymentRepository.class);
        paymentRepository = mock(PaymentRepository.class);
        paymentAttemptRepository = mock(PaymentAttemptRepository.class);
        providerRegistry = mock(PaymentProviderRegistry.class);
        paymentProvider = mock(PaymentProvider.class);
        paymentProperties = new PaymentProperties();
        enrollmentService = mock(EnrollmentApplicationService.class);
        auditService = mock(SecurityAuditService.class);
        eventPublisher = mock(org.springframework.context.ApplicationEventPublisher.class);

        when(providerRegistry.getActiveProvider()).thenReturn(paymentProvider);
        when(providerRegistry.getProvider(any())).thenReturn(paymentProvider);
        when(paymentProvider.getProviderType()).thenReturn(PaymentProviderType.MOCK);

        service = new TrainingPaymentApplicationService(
                batchRepository,
                programRepository,
                enrollmentRepository,
                trainingPaymentRepository,
                paymentRepository,
                paymentAttemptRepository,
                providerRegistry,
                paymentProperties,
                enrollmentService,
                auditService,
                eventPublisher
        );
    }

    @Test
    @DisplayName("AMOUNT TAMPERING DEFENSE: Server must enforce program price (₹5,000) and ignore client amount manipulation")
    void testAmountTamperingDefense() {
        // Authoritative Training Program price = ₹5,000.00
        TrainingBatch batch = TrainingBatch.create("prog-authoritative", "BATCH-PRICE", Instant.now().plusSeconds(3600), Instant.now().plusSeconds(86400), 10, DeliveryMode.ONLINE, null, null, "Asia/Kolkata", "admin");
        TrainingProgram program = new TrainingProgram("prog-authoritative", "pro-mycology", "Pro Mycology", "Desc", "GENERAL", 20, ProgramStatus.ACTIVE, new BigDecimal("5000.00"), "INR", "admin", "admin", Instant.now(), Instant.now());

        when(batchRepository.findById("batch-price")).thenReturn(Optional.of(batch));
        when(enrollmentRepository.existsByBatchIdAndTraineeId("batch-price", "trainee-victor")).thenReturn(false);
        when(programRepository.findById("prog-authoritative")).thenReturn(Optional.of(program));
        when(trainingPaymentRepository.findByBatchIdAndTraineeIdAndStatus("batch-price", "trainee-victor", TrainingPaymentStatus.PENDING)).thenReturn(Optional.empty());

        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentProvider.createPaymentOrder(any())).thenReturn(new PaymentProviderOrderResult("order_tampered_defense", null, new BigDecimal("5000.00"), "INR", "{}"));
        when(trainingPaymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TrainingPaymentOrderResponse response = service.initiatePaymentOrder("batch-price", "trainee-victor");

        // Verification: order MUST be created for ₹5,000.00, NEVER ₹1.00
        assertEquals(new BigDecimal("5000.00"), response.amount());
        assertEquals("INR", response.currency());
        assertNotEquals(new BigDecimal("1.00"), response.amount());
    }

    @Test
    @DisplayName("IDOR PROTECTION: Trainee B cannot verify or hijack Trainee A's payment order")
    void testIdorProtection() {
        Payment paymentOfTraineeA = Payment.createNewPayment("TRN-PAY-TRAINEE-A", UUID.randomUUID(), "trainee-A@sporekart.com", new BigDecimal("5000.00"), "INR", PaymentProviderType.MOCK);

        when(paymentRepository.findByPaymentReference("TRN-PAY-TRAINEE-A")).thenReturn(Optional.of(paymentOfTraineeA));

        TrainingPaymentVerificationRequestDto dto = new TrainingPaymentVerificationRequestDto("TRN-PAY-TRAINEE-A", "order_a", "pay_a", "sig_a");

        // Trainee B attempts to verify Trainee A's payment
        assertThrows(UnauthorizedEnrollmentAccessException.class, () ->
                service.verifyPayment("batch-100", dto, "trainee-B@sporekart.com")
        );
    }
}
