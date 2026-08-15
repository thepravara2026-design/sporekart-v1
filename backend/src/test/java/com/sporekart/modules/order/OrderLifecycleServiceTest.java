package com.sporekart.modules.order;

import com.sporekart.modules.cart.infrastructure.persistence.CartRepository;
import com.sporekart.modules.checkout.application.CheckoutPricingService;
import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.inventory.domain.StockReservation;
import com.sporekart.modules.inventory.infrastructure.persistence.ReservationRepository;
import com.sporekart.modules.order.application.OrderApplicationService;
import com.sporekart.modules.order.application.dto.OrderDto;
import com.sporekart.modules.order.application.dto.OrderTimelineDto;
import com.sporekart.modules.order.domain.AddressSnapshot;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderNumberPort;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.domain.OrderStatusHistory;
import com.sporekart.modules.order.domain.event.OrderLifecycleEvent;
import com.sporekart.modules.order.infrastructure.persistence.OrderRepository;
import com.sporekart.modules.order.infrastructure.persistence.OrderStatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderLifecycleServiceTest {

    private OrderRepository orderRepository;
    private OrderStatusHistoryRepository historyRepository;
    private CartRepository cartRepository;
    private CheckoutPricingService checkoutPricingService;
    private OrderNumberPort orderNumberPort;
    private ReservationRepository reservationRepository;
    private InventoryApplicationService inventoryApplicationService;
    private ApplicationEventPublisher eventPublisher;
    private OrderApplicationService orderApplicationService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        historyRepository = mock(OrderStatusHistoryRepository.class);
        cartRepository = mock(CartRepository.class);
        checkoutPricingService = mock(CheckoutPricingService.class);
        orderNumberPort = mock(OrderNumberPort.class);
        reservationRepository = mock(ReservationRepository.class);
        inventoryApplicationService = mock(InventoryApplicationService.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        orderApplicationService = new OrderApplicationService(
                orderRepository, historyRepository, cartRepository,
                checkoutPricingService, orderNumberPort, reservationRepository,
                inventoryApplicationService, eventPublisher
        );
    }

    @Test
    @DisplayName("Should confirm order payment and record status history entry")
    void testConfirmOrderPayment() {
        UUID orderId = UUID.randomUUID();
        AddressSnapshot address = new AddressSnapshot("John", "123", "Line1", null, "City", "State", "100", "India");
        Order order = new Order(
                orderId, "SPK-10001", "cust-100", OrderStatus.CREATED, "INR",
                new BigDecimal("1000.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("1000.00"), null, address, null, List.of(), OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderDto dto = orderApplicationService.confirmOrderPayment(orderId, "PAY-REF-123");

        assertNotNull(dto);
        assertEquals(OrderStatus.CONFIRMED, dto.status());
        verify(historyRepository).save(any(OrderStatusHistory.class));
        verify(eventPublisher).publishEvent(any(OrderLifecycleEvent.class));
    }

    @Test
    @DisplayName("Should cancel order, record history, and release inventory reservation")
    void testCancelOrderWithInventoryRelease() {
        UUID orderId = UUID.randomUUID();
        String customerId = "cust-100";
        AddressSnapshot address = new AddressSnapshot("John", "123", "Line1", null, "City", "State", "100", "India");
        Order order = new Order(
                orderId, "SPK-10002", customerId, OrderStatus.CREATED, "INR",
                new BigDecimal("1000.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("1000.00"), null, address, null, List.of(), OffsetDateTime.now(), OffsetDateTime.now()
        );

        UUID resId = UUID.randomUUID();
        StockReservation reservation = new StockReservation(
                resId, "RES-001", orderId, com.sporekart.modules.inventory.domain.ReservationStatus.ACTIVE,
                OffsetDateTime.now().plusMinutes(15), null, List.of(), OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(orderRepository.findByIdAndCustomerId(orderId, customerId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reservationRepository.findByOrderId(orderId)).thenReturn(Optional.of(reservation));

        OrderDto dto = orderApplicationService.cancelOrder(customerId, orderId, "Customer changed mind");

        assertNotNull(dto);
        assertEquals(OrderStatus.CANCELLED, dto.status());
        verify(inventoryApplicationService).releaseReservation(eq(resId), any());
        verify(historyRepository).save(any(OrderStatusHistory.class));
    }

    @Test
    @DisplayName("Should retrieve order timeline audit history")
    void testGetOrderTimeline() {
        UUID orderId = UUID.randomUUID();
        String orderNumber = "SPK-10003";
        String customerId = "cust-100";

        AddressSnapshot address = new AddressSnapshot("John", "123", "Line1", null, "City", "State", "100", "India");
        Order order = new Order(
                orderId, orderNumber, customerId, OrderStatus.CONFIRMED, "INR",
                new BigDecimal("1000.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("1000.00"), null, address, null, List.of(), OffsetDateTime.now(), OffsetDateTime.now()
        );

        OrderStatusHistory h1 = OrderStatusHistory.recordTransition(orderId, null, OrderStatus.CREATED, "Created", com.sporekart.modules.order.domain.OrderActorType.CUSTOMER, customerId, null);
        OrderStatusHistory h2 = OrderStatusHistory.recordTransition(orderId, OrderStatus.CREATED, OrderStatus.CONFIRMED, "Payment confirmed", com.sporekart.modules.order.domain.OrderActorType.PAYMENT, "PAY-1", null);

        when(orderRepository.findByOrderNumberAndCustomerId(orderNumber, customerId)).thenReturn(Optional.of(order));
        when(historyRepository.findByOrderId(orderId)).thenReturn(List.of(h1, h2));

        OrderTimelineDto timeline = orderApplicationService.getOrderTimeline(customerId, orderNumber);

        assertNotNull(timeline);
        assertEquals(orderNumber, timeline.orderNumber());
        assertEquals(2, timeline.history().size());
        assertEquals(OrderStatus.CONFIRMED, timeline.currentStatus());
    }
}
