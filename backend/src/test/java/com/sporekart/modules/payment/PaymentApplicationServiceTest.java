package com.sporekart.modules.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.inventory.domain.StockReservation;
import com.sporekart.modules.inventory.infrastructure.persistence.ReservationRepository;
import com.sporekart.modules.order.domain.AddressSnapshot;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.infrastructure.persistence.OrderRepository;
import com.sporekart.modules.payment.application.PaymentApplicationService;
import com.sporekart.modules.payment.application.dto.PaymentCheckoutDto;
import com.sporekart.modules.payment.application.dto.PaymentDto;
import com.sporekart.modules.payment.application.dto.PaymentVerificationCommand;
import com.sporekart.modules.payment.domain.Payment;
import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.payment.domain.PaymentStatus;
import com.sporekart.modules.payment.domain.exception.OrderNotPayableException;
import com.sporekart.modules.payment.domain.exception.PaymentVerificationFailedException;
import com.sporekart.modules.payment.infrastructure.config.PaymentProperties;
import com.sporekart.modules.payment.infrastructure.persistence.PaymentAttemptRepository;
import com.sporekart.modules.payment.infrastructure.persistence.PaymentRepository;
import com.sporekart.modules.payment.infrastructure.persistence.WebhookEventRepository;
import com.sporekart.modules.payment.infrastructure.provider.PaymentProviderRegistry;
import com.sporekart.modules.payment.infrastructure.provider.mock.MockPaymentProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentApplicationServiceTest {

    private PaymentRepository paymentRepository;
    private PaymentAttemptRepository paymentAttemptRepository;
    private WebhookEventRepository webhookEventRepository;
    private OrderRepository orderRepository;
    private ReservationRepository reservationRepository;
    private InventoryApplicationService inventoryApplicationService;
    private PaymentProviderRegistry providerRegistry;
    private PaymentProperties paymentProperties;
    private PaymentApplicationService paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        paymentAttemptRepository = mock(PaymentAttemptRepository.class);
        webhookEventRepository = mock(WebhookEventRepository.class);
        orderRepository = mock(OrderRepository.class);
        reservationRepository = mock(ReservationRepository.class);
        inventoryApplicationService = mock(InventoryApplicationService.class);
        paymentProperties = new PaymentProperties();

        MockPaymentProvider mockProvider = new MockPaymentProvider();
        providerRegistry = new PaymentProviderRegistry(List.of(mockProvider), paymentProperties);

        paymentService = new PaymentApplicationService(
                paymentRepository, paymentAttemptRepository, webhookEventRepository,
                orderRepository, reservationRepository, inventoryApplicationService,
                providerRegistry, paymentProperties, new ObjectMapper()
        );
    }

    @Test
    @DisplayName("Should create payment attempt when order is CREATED and active reservation exists")
    void testCreatePaymentSuccess() {
        UUID orderId = UUID.randomUUID();
        String customerId = "cust-100";

        AddressSnapshot address = new AddressSnapshot("John", "123", "Main", null, "City", "State", "100", "India");
        Order order = new Order(
                orderId, "SPK-10001", customerId, OrderStatus.CREATED, "INR",
                new BigDecimal("1000.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("1000.00"), null, address, null, List.of(), OffsetDateTime.now(), OffsetDateTime.now()
        );

        StockReservation reservation = StockReservation.createActiveReservation(
                "RES-10001", orderId, OffsetDateTime.now().plusMinutes(15), List.of()
        );

        when(orderRepository.findByIdAndCustomerId(orderId, customerId)).thenReturn(Optional.of(order));
        when(reservationRepository.findByOrderId(orderId)).thenReturn(Optional.of(reservation));
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentCheckoutDto checkout = paymentService.createPayment(orderId, customerId);

        assertNotNull(checkout);
        assertEquals(orderId, checkout.orderId());
        assertEquals(new BigDecimal("1000.00"), checkout.amount());
        assertEquals(PaymentProviderType.MOCK, checkout.provider());
        assertNotNull(checkout.providerOrderId());

        verify(paymentRepository, org.mockito.Mockito.atLeastOnce()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should reject payment creation when inventory reservation is missing or expired")
    void testCreatePaymentWithoutReservationFails() {
        UUID orderId = UUID.randomUUID();
        String customerId = "cust-100";

        AddressSnapshot address = new AddressSnapshot("John", "123", "Main", null, "City", "State", "100", "India");
        Order order = new Order(
                orderId, "SPK-10002", customerId, OrderStatus.CREATED, "INR",
                new BigDecimal("1000.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("1000.00"), null, address, null, List.of(), OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(orderRepository.findByIdAndCustomerId(orderId, customerId)).thenReturn(Optional.of(order));
        when(reservationRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotPayableException.class, () -> paymentService.createPayment(orderId, customerId));
    }

    @Test
    @DisplayName("Should verify payment callback, transition payment to SUCCESS and mark Order PAID")
    void testVerifyPaymentSuccess() {
        UUID orderId = UUID.randomUUID();
        String customerId = "cust-100";
        String payRef = "PAY-SPK-10001";

        Payment payment = Payment.createNewPayment(payRef, orderId, customerId, new BigDecimal("1000.00"), "INR", PaymentProviderType.MOCK);
        var attempt = payment.createAttempt(payRef + "-ATT-1");
        attempt.updateProviderDetails("order_mock_123", null, null);

        AddressSnapshot address = new AddressSnapshot("John", "123", "Main", null, "City", "State", "100", "India");
        Order order = new Order(
                orderId, "SPK-10001", customerId, OrderStatus.CREATED, "INR",
                new BigDecimal("1000.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("1000.00"), null, address, null, List.of(), OffsetDateTime.now(), OffsetDateTime.now()
        );

        StockReservation reservation = StockReservation.createActiveReservation("RES-10001", orderId, OffsetDateTime.now().plusMinutes(15), List.of());

        when(paymentRepository.findByPaymentReference(payRef)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(reservationRepository.findByOrderId(orderId)).thenReturn(Optional.of(reservation));

        PaymentVerificationCommand command = new PaymentVerificationCommand(payRef, "order_mock_123", "pay_mock_999", "sig_valid_123");
        PaymentDto result = paymentService.verifyPayment(command, customerId);

        assertNotNull(result);
        assertEquals(PaymentStatus.SUCCESS, result.status());
        assertEquals(OrderStatus.PAID, order.getStatus());

        verify(orderRepository).save(order);
        verify(reservationRepository).save(reservation);
    }

    @Test
    @DisplayName("Should fail payment verification when provider signature is invalid")
    void testVerifyPaymentInvalidSignature() {
        UUID orderId = UUID.randomUUID();
        String customerId = "cust-100";
        String payRef = "PAY-SPK-10002";

        Payment payment = Payment.createNewPayment(payRef, orderId, customerId, new BigDecimal("1000.00"), "INR", PaymentProviderType.MOCK);
        var attempt = payment.createAttempt(payRef + "-ATT-1");
        attempt.updateProviderDetails("order_mock_123", null, null);

        when(paymentRepository.findByPaymentReference(payRef)).thenReturn(Optional.of(payment));

        PaymentVerificationCommand command = new PaymentVerificationCommand(payRef, "order_mock_123", "pay_mock_999", "INVALID_SIGNATURE");
        assertThrows(PaymentVerificationFailedException.class, () -> paymentService.verifyPayment(command, customerId));
        assertEquals(PaymentStatus.FAILED, payment.getStatus());
    }
}
