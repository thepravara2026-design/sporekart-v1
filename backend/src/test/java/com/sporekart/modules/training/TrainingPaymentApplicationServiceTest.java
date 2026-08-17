package com.sporekart.modules.training;

import com.sporekart.modules.payment.domain.Payment;
import com.sporekart.modules.payment.domain.PaymentAttempt;
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
import com.sporekart.modules.training.controller.dto.TrainingPaymentStatusResponse;
import com.sporekart.modules.training.controller.dto.TrainingPaymentVerificationRequestDto;
import com.sporekart.modules.training.domain.*;
import com.sporekart.modules.training.domain.exception.*;
import com.sporekart.modules.training.domain.port.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TrainingPaymentApplicationServiceTest {

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
    @DisplayName("Should successfully initiate payment order for paid batch")
    void testInitiatePaymentOrderSuccess() {
        TrainingBatch batch = TrainingBatch.create("prog-1", "BATCH-100", Instant.now().plusSeconds(3600), Instant.now().plusSeconds(86400), 10, DeliveryMode.ONLINE, null, null, "Asia/Kolkata", "admin");
        TrainingProgram program = new TrainingProgram("prog-1", "mycology-101", "Mycology 101", "Desc", "GENERAL", 10, ProgramStatus.ACTIVE, new BigDecimal("5000.00"), "INR", "admin", "admin", Instant.now(), Instant.now());

        when(batchRepository.findById("batch-100")).thenReturn(Optional.of(batch));
        when(enrollmentRepository.existsByBatchIdAndTraineeId("batch-100", "trainee-1")).thenReturn(false);
        when(programRepository.findById("prog-1")).thenReturn(Optional.of(program));
        when(trainingPaymentRepository.findByBatchIdAndTraineeIdAndStatus("batch-100", "trainee-1", TrainingPaymentStatus.PENDING)).thenReturn(Optional.empty());

        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentProvider.createPaymentOrder(any())).thenReturn(new PaymentProviderOrderResult("order_mock_123", null, new BigDecimal("5000.00"), "INR", "{}"));
        when(trainingPaymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TrainingPaymentOrderResponse response = service.initiatePaymentOrder("batch-100", "trainee-1");

        assertNotNull(response);
        assertEquals("batch-100", response.batchId());
        assertEquals(new BigDecimal("5000.00"), response.amount());
        assertEquals("INR", response.currency());
        assertEquals("order_mock_123", response.providerOrderId());
    }

    @Test
    @DisplayName("Should reject payment order creation if batch is FULL")
    void testInitiatePaymentOrderFullBatch() {
        TrainingBatch batch = TrainingBatch.create("prog-1", "BATCH-FULL", Instant.now().plusSeconds(3600), Instant.now().plusSeconds(86400), 2, DeliveryMode.ONLINE, null, null, "Asia/Kolkata", "admin");
        batch.allocateSeat();
        batch.allocateSeat(); // Occupied = 2/2 -> FULL

        when(batchRepository.findById("batch-full")).thenReturn(Optional.of(batch));

        assertThrows(BatchFullException.class, () -> service.initiatePaymentOrder("batch-full", "trainee-1"));
    }

    @Test
    @DisplayName("Should reject payment order creation if training program is free (price=0)")
    void testInitiatePaymentOrderFreeProgram() {
        TrainingBatch batch = TrainingBatch.create("prog-free", "BATCH-FREE", Instant.now().plusSeconds(3600), Instant.now().plusSeconds(86400), 10, DeliveryMode.ONLINE, null, null, "Asia/Kolkata", "admin");
        TrainingProgram program = new TrainingProgram("prog-free", "free-intro", "Free Intro", "Desc", "GENERAL", 10, ProgramStatus.ACTIVE, BigDecimal.ZERO, "INR", "admin", "admin", Instant.now(), Instant.now());

        when(batchRepository.findById("batch-free")).thenReturn(Optional.of(batch));
        when(enrollmentRepository.existsByBatchIdAndTraineeId("batch-free", "trainee-1")).thenReturn(false);
        when(programRepository.findById("prog-free")).thenReturn(Optional.of(program));

        assertThrows(InvalidPaymentStateException.class, () -> service.initiatePaymentOrder("batch-free", "trainee-1"));
    }

    @Test
    @DisplayName("Should verify valid payment signature, confirm enrollment, and return status")
    void testVerifyPaymentSuccess() {
        Payment payment = Payment.createNewPayment("TRN-PAY-001", UUID.randomUUID(), "trainee-1", new BigDecimal("5000.00"), "INR", PaymentProviderType.MOCK);
        PaymentAttempt attempt = payment.createAttempt("TRN-PAY-001-ATT-1");
        attempt.updateProviderDetails("order_mock_123", "pay_mock_999", null);

        TrainingEnrollmentPayment trnPayment = TrainingEnrollmentPayment.create(payment.getId().toString(), "batch-100", "trainee-1", new BigDecimal("5000.00"), "INR", "trainee-1");
        TrainingEnrollment enrollment = TrainingEnrollment.create("batch-100", "trainee-1", "PAYMENT-TRN-PAY-001", "trainee-1");

        when(paymentRepository.findByPaymentReference("TRN-PAY-001")).thenReturn(Optional.of(payment));
        when(trainingPaymentRepository.findByPaymentId(payment.getId().toString())).thenReturn(Optional.of(trnPayment));
        when(paymentProvider.verifyPaymentSignature(any())).thenReturn(true);
        when(enrollmentService.enrollTrainee(eq("batch-100"), eq("trainee-1"), anyString())).thenReturn(enrollment);
        when(trainingPaymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TrainingPaymentVerificationRequestDto dto = new TrainingPaymentVerificationRequestDto("TRN-PAY-001", "order_mock_123", "pay_mock_999", "valid_signature");
        TrainingPaymentStatusResponse response = service.verifyPayment("batch-100", dto, "trainee-1");

        assertEquals(TrainingPaymentStatus.ENROLLMENT_CONFIRMED, response.status());
        assertNotNull(response.enrollmentId());
    }

    @Test
    @DisplayName("Should fail payment verification when provider signature is invalid")
    void testVerifyPaymentInvalidSignature() {
        Payment payment = Payment.createNewPayment("TRN-PAY-001", UUID.randomUUID(), "trainee-1", new BigDecimal("5000.00"), "INR", PaymentProviderType.MOCK);
        payment.createAttempt("TRN-PAY-001-ATT-1");

        TrainingEnrollmentPayment trnPayment = TrainingEnrollmentPayment.create(payment.getId().toString(), "batch-100", "trainee-1", new BigDecimal("5000.00"), "INR", "trainee-1");

        when(paymentRepository.findByPaymentReference("TRN-PAY-001")).thenReturn(Optional.of(payment));
        when(trainingPaymentRepository.findByPaymentId(payment.getId().toString())).thenReturn(Optional.of(trnPayment));
        when(paymentProvider.verifyPaymentSignature(any())).thenReturn(false);
        when(trainingPaymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TrainingPaymentVerificationRequestDto dto = new TrainingPaymentVerificationRequestDto("TRN-PAY-001", "order_mock_123", "pay_mock_999", "INVALID_SIGNATURE");
        assertThrows(PaymentVerificationException.class, () -> service.verifyPayment("batch-100", dto, "trainee-1"));
        assertEquals(TrainingPaymentStatus.FAILED, trnPayment.getStatus());
    }
}
