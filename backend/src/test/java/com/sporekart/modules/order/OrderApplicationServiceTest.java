package com.sporekart.modules.order;

import com.sporekart.modules.cart.domain.Cart;
import com.sporekart.modules.cart.domain.CartStatus;
import com.sporekart.modules.cart.infrastructure.persistence.CartRepository;
import com.sporekart.modules.checkout.application.CheckoutPricingService;
import com.sporekart.modules.checkout.domain.exception.CartEmptyException;
import com.sporekart.modules.checkout.domain.model.CheckoutLineItem;
import com.sporekart.modules.checkout.domain.model.CheckoutPreview;
import com.sporekart.modules.checkout.domain.model.Money;
import com.sporekart.modules.order.application.OrderApplicationService;
import com.sporekart.modules.order.application.dto.AddressDto;
import com.sporekart.modules.order.application.dto.CreateOrderCommand;
import com.sporekart.modules.order.application.dto.OrderDto;
import com.sporekart.modules.order.domain.AddressSnapshot;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderNumberPort;
import com.sporekart.modules.order.domain.OrderStatus;

import com.sporekart.modules.order.infrastructure.persistence.OrderRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderApplicationServiceTest {

    private OrderRepository orderRepository;
    private CartRepository cartRepository;
    private CheckoutPricingService checkoutPricingService;
    private OrderNumberPort orderNumberPort;
    private OrderApplicationService orderApplicationService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        cartRepository = mock(CartRepository.class);
        checkoutPricingService = mock(CheckoutPricingService.class);
        orderNumberPort = mock(OrderNumberPort.class);
        orderApplicationService = new OrderApplicationService(
                orderRepository, cartRepository, checkoutPricingService, orderNumberPort
        );
    }

    @Test
    @DisplayName("Should create order successfully from active cart using Sprint 3B pricing engine")
    void testCreateOrderSuccess() {
        String customerId = "cust-100";
        Cart activeCart = Cart.createNewActiveCart(customerId, "INR");
        activeCart.addItem(UUID.randomUUID(), null, "SKU-01", "Mushroom Spawn", null, new BigDecimal("500.00"), 2, 10, 100);

        when(cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)).thenReturn(Optional.of(activeCart));
        when(orderNumberPort.generateOrderNumber()).thenReturn("SPK-20260815-100001");

        UUID productId = UUID.randomUUID();
        CheckoutLineItem lineItem = new CheckoutLineItem(
                UUID.randomUUID(), productId, "SKU-01", "Mushroom Spawn", 2,
                Money.of(new BigDecimal("500.00"), "INR"), Money.of(new BigDecimal("500.00"), "INR"), false,
                Money.of(new BigDecimal("1000.00"), "INR"), Money.zero("INR"), Money.of(new BigDecimal("180.00"), "INR"),
                Money.of(new BigDecimal("1180.00"), "INR")
        );

        CheckoutPreview preview = new CheckoutPreview(
                UUID.randomUUID(), activeCart.getId(), customerId, "INR", List.of(lineItem),
                Money.of(new BigDecimal("1000.00"), "INR"), Money.zero("INR"), Money.of(new BigDecimal("180.00"), "INR"),
                Money.of(new BigDecimal("50.00"), "INR"), Money.of(new BigDecimal("1230.00"), "INR"), List.of(), OffsetDateTime.now()
        );

        when(checkoutPricingService.calculateCheckoutPreview(eq(activeCart), anyString(), eq(null))).thenReturn(preview);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddressDto addressDto = new AddressDto("Jane Doe", "9876543210", "456 Oak Lane", null, "Mumbai", "Maharashtra", "400001", "India");
        CreateOrderCommand command = new CreateOrderCommand(addressDto, "KEY-999", "Fast delivery");

        OrderDto result = orderApplicationService.createOrder(customerId, command);

        assertNotNull(result);
        assertEquals("SPK-20260815-100001", result.orderNumber());
        assertEquals(customerId, result.customerId());
        assertEquals(OrderStatus.CREATED, result.status());
        assertEquals(new BigDecimal("1230.00"), result.grandTotal());
        assertEquals(CartStatus.CHECKED_OUT, activeCart.getStatus());

        verify(cartRepository).save(activeCart);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should replay existing order when idempotency key matches")
    void testCreateOrderIdempotencyReplay() {
        String customerId = "cust-100";
        String idempotencyKey = "IDEM-12345";
        AddressSnapshot address = new AddressSnapshot("Jane", "999", "Line", null, "City", "State", "100001", "India");

        Order existingOrder = new Order(
                UUID.randomUUID(), "SPK-20260815-000099", customerId, OrderStatus.CREATED, "INR",
                new BigDecimal("500.00"), BigDecimal.ZERO, new BigDecimal("90.00"), new BigDecimal("50.00"),
                new BigDecimal("640.00"), idempotencyKey, address, null, List.of(), OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(orderRepository.findByCustomerIdAndIdempotencyKey(customerId, idempotencyKey)).thenReturn(Optional.of(existingOrder));

        AddressDto addressDto = new AddressDto("Jane", "999", "Line", null, "City", "State", "100001", "India");
        CreateOrderCommand command = new CreateOrderCommand(addressDto, idempotencyKey, null);

        OrderDto result = orderApplicationService.createOrder(customerId, command);

        assertNotNull(result);
        assertEquals("SPK-20260815-000099", result.orderNumber());
        verify(cartRepository, never()).findByCustomerIdAndStatus(anyString(), any());
        verify(checkoutPricingService, never()).calculateCheckoutPreview(any(), any(), any());
    }

    @Test
    @DisplayName("Should throw CartEmptyException when customer cart has no items")
    void testCreateOrderEmptyCart() {
        String customerId = "cust-200";
        Cart emptyCart = Cart.createNewActiveCart(customerId, "INR");

        when(cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)).thenReturn(Optional.of(emptyCart));

        AddressDto addressDto = new AddressDto("Jane", "999", "Line", null, "City", "State", "100001", "India");
        CreateOrderCommand command = new CreateOrderCommand(addressDto, null, null);

        assertThrows(CartEmptyException.class, () -> orderApplicationService.createOrder(customerId, command));
    }

    @Test
    @DisplayName("Should cancel eligible order successfully")
    void testCancelOrder() {
        String customerId = "cust-1";
        UUID orderId = UUID.randomUUID();
        AddressSnapshot address = new AddressSnapshot("Jane", "999", "Line", null, "City", "State", "100001", "India");

        Order order = new Order(
                orderId, "SPK-001", customerId, OrderStatus.CREATED, "INR",
                new BigDecimal("100.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("100.00"), null, address, null, List.of(), OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(orderRepository.findByIdAndCustomerId(orderId, customerId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderDto cancelled = orderApplicationService.cancelOrder(customerId, orderId);

        assertNotNull(cancelled);
        assertEquals(OrderStatus.CANCELLED, cancelled.status());
    }
}
