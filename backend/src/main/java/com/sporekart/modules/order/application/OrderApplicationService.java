package com.sporekart.modules.order.application;

import com.sporekart.modules.cart.domain.Cart;
import com.sporekart.modules.cart.domain.CartStatus;
import com.sporekart.modules.cart.domain.exception.CartNotFoundException;
import com.sporekart.modules.cart.infrastructure.persistence.CartRepository;
import com.sporekart.modules.checkout.application.CheckoutPricingService;
import com.sporekart.modules.checkout.domain.exception.CartEmptyException;
import com.sporekart.modules.checkout.domain.model.CheckoutLineItem;
import com.sporekart.modules.checkout.domain.model.CheckoutPreview;
import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.inventory.domain.StockReservation;
import com.sporekart.modules.inventory.infrastructure.persistence.ReservationRepository;
import com.sporekart.modules.order.application.dto.CreateOrderCommand;
import com.sporekart.modules.order.application.dto.OrderDto;
import com.sporekart.modules.order.application.dto.OrderSummaryDto;
import com.sporekart.modules.order.application.dto.OrderTimelineDto;
import com.sporekart.modules.order.domain.AddressSnapshot;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderActorType;
import com.sporekart.modules.order.domain.OrderItem;
import com.sporekart.modules.order.domain.OrderNumberPort;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.domain.OrderStatusHistory;
import com.sporekart.modules.order.domain.event.OrderLifecycleEvent;
import com.sporekart.modules.order.domain.exception.OrderAccessDeniedException;
import com.sporekart.modules.order.domain.exception.OrderNotFoundException;
import com.sporekart.modules.order.infrastructure.persistence.OrderRepository;
import com.sporekart.modules.order.infrastructure.persistence.OrderStatusHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderApplicationService {

    private static final Logger log = LoggerFactory.getLogger(OrderApplicationService.class);

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final CartRepository cartRepository;
    private final CheckoutPricingService checkoutPricingService;
    private final OrderNumberPort orderNumberPort;
    private final ReservationRepository reservationRepository;
    private final InventoryApplicationService inventoryApplicationService;
    private final ApplicationEventPublisher eventPublisher;

    public OrderApplicationService(
            OrderRepository orderRepository,
            OrderStatusHistoryRepository historyRepository,
            CartRepository cartRepository,
            CheckoutPricingService checkoutPricingService,
            OrderNumberPort orderNumberPort,
            ReservationRepository reservationRepository,
            InventoryApplicationService inventoryApplicationService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.orderRepository = orderRepository;
        this.historyRepository = historyRepository;
        this.cartRepository = cartRepository;
        this.checkoutPricingService = checkoutPricingService;
        this.orderNumberPort = orderNumberPort;
        this.reservationRepository = reservationRepository;
        this.inventoryApplicationService = inventoryApplicationService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public OrderDto createOrder(String customerId, CreateOrderCommand command) {
        log.info("Initiating order creation for customerId: {}", customerId);

        // 1. Check idempotency replay
        if (command.idempotencyKey() != null && !command.idempotencyKey().isBlank()) {
            Optional<Order> existingOrder = orderRepository.findByCustomerIdAndIdempotencyKey(customerId, command.idempotencyKey());
            if (existingOrder.isPresent()) {
                log.info("Idempotent replay match for customerId: {} and idempotencyKey: {}", customerId, command.idempotencyKey());
                return OrderDto.fromDomain(existingOrder.get());
            }
        }

        // 2. Retrieve active cart
        Cart activeCart = cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                .orElseThrow(() -> new CartNotFoundException(customerId));

        if (activeCart.getItems().isEmpty()) {
            throw new CartEmptyException(activeCart.getId());
        }

        // 3. Compute authoritative pricing via Sprint 3B CheckoutPricingService
        AddressSnapshot addressSnapshot = command.shippingAddress().toDomain();
        String destinationAddress = addressSnapshot.getAddressLine1() + ", " + addressSnapshot.getCity();
        CheckoutPreview preview = checkoutPricingService.calculateCheckoutPreview(activeCart, destinationAddress, null);

        // 4. Build line item snapshots
        UUID orderId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        List<OrderItem> orderItems = new ArrayList<>();
        for (CheckoutLineItem line : preview.getItems()) {
            orderItems.add(new OrderItem(
                    UUID.randomUUID(),
                    orderId,
                    line.getProductId(),
                    null,
                    line.getSku(),
                    line.getProductName(),
                    null,
                    line.getAuthoritativeUnitPrice().getAmount(),
                    line.getQuantity(),
                    line.getDiscountAmount().getAmount(),
                    line.getTaxAmount().getAmount(),
                    line.getLineSubtotal().getAmount(),
                    line.getLineTotal().getAmount(),
                    now
            ));
        }

        // 5. Generate unique order number
        String orderNumber = orderNumberPort.generateOrderNumber();

        // 6. Create Order Aggregate
        Order order = new Order(
                orderId,
                orderNumber,
                customerId,
                OrderStatus.CREATED,
                preview.getGrandTotal().getCurrency(),
                preview.getSubtotal().getAmount(),
                preview.getDiscountTotal().getAmount(),
                preview.getTaxTotal().getAmount(),
                preview.getShippingFee().getAmount(),
                preview.getGrandTotal().getAmount(),
                command.idempotencyKey(),
                addressSnapshot,
                command.customerNotes(),
                orderItems,
                0L,
                now,
                now
        );

        // 7. Persist order & initial status history
        Order savedOrder = orderRepository.save(order);
        recordHistory(savedOrder.getId(), null, OrderStatus.CREATED, "Order created from checkout", OrderActorType.CUSTOMER, customerId, command.idempotencyKey());

        // 8. Transition cart to CHECKED_OUT
        activeCart.markAsCheckedOut();
        cartRepository.save(activeCart);

        // 9. Publish event
        eventPublisher.publishEvent(OrderLifecycleEvent.create(
                savedOrder.getId(), savedOrder.getOrderNumber(), null, OrderStatus.CREATED,
                "Order created from checkout", OrderActorType.CUSTOMER, customerId, command.idempotencyKey()
        ));

        log.info("Successfully created order {} (number: {}) for customer {}", savedOrder.getId(), savedOrder.getOrderNumber(), customerId);
        return OrderDto.fromDomain(savedOrder);
    }

    @Transactional
    public OrderDto confirmOrderPayment(UUID orderId, String paymentReference) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() == OrderStatus.CONFIRMED || order.getStatus() == OrderStatus.PAID) {
            return OrderDto.fromDomain(order); // Idempotent
        }

        OrderStatus prev = order.getStatus();
        order.markPaid();
        Order saved = orderRepository.save(order);

        recordHistory(orderId, prev, OrderStatus.CONFIRMED, "Payment confirmed: " + paymentReference, OrderActorType.PAYMENT, paymentReference, null);
        eventPublisher.publishEvent(OrderLifecycleEvent.create(orderId, order.getOrderNumber(), prev, OrderStatus.CONFIRMED, "Payment confirmed", OrderActorType.PAYMENT, paymentReference, null));

        log.info("Order {} transitioned from {} to CONFIRMED via payment {}", orderId, prev, paymentReference);
        return OrderDto.fromDomain(saved);
    }

    @Transactional
    public OrderDto cancelOrder(String customerId, UUID orderId, String reason) {
        Order order = orderRepository.findByIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return OrderDto.fromDomain(order); // Idempotent
        }

        OrderStatus prev = order.getStatus();
        order.cancel();
        Order saved = orderRepository.save(order);

        String actualReason = (reason != null && !reason.isBlank()) ? reason : "Customer cancelled order";
        recordHistory(orderId, prev, OrderStatus.CANCELLED, actualReason, OrderActorType.CUSTOMER, customerId, null);
        releaseInventoryIfPresent(orderId, actualReason);

        eventPublisher.publishEvent(OrderLifecycleEvent.create(orderId, order.getOrderNumber(), prev, OrderStatus.CANCELLED, actualReason, OrderActorType.CUSTOMER, customerId, null));
        log.info("Order {} cancelled by customer {}", orderId, customerId);
        return OrderDto.fromDomain(saved);
    }

    @Transactional
    public OrderDto cancelOrderAsAdmin(String adminId, UUID orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return OrderDto.fromDomain(order);
        }

        OrderStatus prev = order.getStatus();
        order.cancel();
        Order saved = orderRepository.save(order);

        String actualReason = "Admin cancellation: " + ((reason != null && !reason.isBlank()) ? reason : "Administrative override");
        recordHistory(orderId, prev, OrderStatus.CANCELLED, actualReason, OrderActorType.ADMIN, adminId, null);
        releaseInventoryIfPresent(orderId, actualReason);

        eventPublisher.publishEvent(OrderLifecycleEvent.create(orderId, order.getOrderNumber(), prev, OrderStatus.CANCELLED, actualReason, OrderActorType.ADMIN, adminId, null));
        log.info("Order {} cancelled by admin {}", orderId, adminId);
        return OrderDto.fromDomain(saved);
    }

    @Transactional
    public void expireOrder(UUID orderId, String reason) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || order.isTerminal()) {
            return;
        }

        OrderStatus prev = order.getStatus();
        order.expire();
        orderRepository.save(order);

        String actualReason = (reason != null && !reason.isBlank()) ? reason : "Order reservation / payment expired";
        recordHistory(orderId, prev, OrderStatus.EXPIRED, actualReason, OrderActorType.SYSTEM, "SYSTEM", null);
        releaseInventoryIfPresent(orderId, actualReason);

        eventPublisher.publishEvent(OrderLifecycleEvent.create(orderId, order.getOrderNumber(), prev, OrderStatus.EXPIRED, actualReason, OrderActorType.SYSTEM, "SYSTEM", null));
        log.info("Order {} expired cleanly by system", orderId);
    }

    @Transactional
    public OrderDto startProcessing(UUID orderId, String adminId) {
        return transitionOrder(orderId, OrderStatus.PROCESSING, "Warehouse processing started", OrderActorType.ADMIN, adminId);
    }

    @Transactional
    public OrderDto markReadyForFulfilment(UUID orderId, String adminId) {
        return transitionOrder(orderId, OrderStatus.READY_FOR_FULFILMENT, "Order ready for fulfilment and shipping handoff", OrderActorType.ADMIN, adminId);
    }

    @Transactional
    public OrderDto markShipped(UUID orderId, String adminId, String trackingNumber) {
        String reason = "Package dispatched via courier" + (trackingNumber != null ? " (Tracking: " + trackingNumber + ")" : "");
        return transitionOrder(orderId, OrderStatus.SHIPPED, reason, OrderActorType.SHIPPING, adminId);
    }

    @Transactional
    public OrderDto markOutForDelivery(UUID orderId, String adminId) {
        return transitionOrder(orderId, OrderStatus.OUT_FOR_DELIVERY, "Package out for final delivery", OrderActorType.SHIPPING, adminId);
    }

    @Transactional
    public OrderDto markDelivered(UUID orderId, String adminId) {
        return transitionOrder(orderId, OrderStatus.DELIVERED, "Package delivered to customer", OrderActorType.SHIPPING, adminId);
    }

    @Transactional
    public OrderDto markCompleted(UUID orderId, String adminId) {
        return transitionOrder(orderId, OrderStatus.COMPLETED, "Order lifecycle completed successfully", OrderActorType.ADMIN, adminId);
    }

    private OrderDto transitionOrder(UUID orderId, OrderStatus targetStatus, String reason, OrderActorType actorType, String actorId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() == targetStatus) {
            return OrderDto.fromDomain(order); // Idempotent
        }

        OrderStatus prev = order.getStatus();
        switch (targetStatus) {
            case PROCESSING -> order.startProcessing();
            case READY_FOR_FULFILMENT -> order.markReadyForFulfilment();
            case SHIPPED -> order.markShipped();
            case OUT_FOR_DELIVERY -> order.markOutForDelivery();
            case DELIVERED -> order.markDelivered();
            case COMPLETED -> order.markCompleted();
            default -> throw new IllegalArgumentException("Unsupported operational transition: " + targetStatus);
        }

        Order saved = orderRepository.save(order);
        recordHistory(orderId, prev, targetStatus, reason, actorType, actorId, null);
        eventPublisher.publishEvent(OrderLifecycleEvent.create(orderId, order.getOrderNumber(), prev, targetStatus, reason, actorType, actorId, null));

        log.info("Order {} transitioned from {} to {}", orderId, prev, targetStatus);
        return OrderDto.fromDomain(saved);
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryDto> getOrderHistory(String customerId, Pageable pageable) {
        return orderRepository.findByCustomerId(customerId, pageable)
                .map(OrderSummaryDto::fromDomain);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderDetail(String customerId, UUID orderId) {
        return orderRepository.findByIdAndCustomerId(orderId, customerId)
                .map(OrderDto::fromDomain)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderDetail(String customerId, String orderNumber) {
        return orderRepository.findByOrderNumberAndCustomerId(orderNumber, customerId)
                .map(OrderDto::fromDomain)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with number: " + orderNumber));
    }

    @Transactional(readOnly = true)
    public OrderTimelineDto getOrderTimeline(String customerId, String orderNumber) {
        Order order = orderRepository.findByOrderNumberAndCustomerId(orderNumber, customerId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with number: " + orderNumber));

        List<OrderStatusHistory> history = historyRepository.findByOrderId(order.getId());
        return OrderTimelineDto.fromDomain(order, history);
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryDto> getAdminOrderList(Pageable pageable, OrderStatus statusFilter) {
        if (statusFilter != null) {
            return orderRepository.findByStatus(statusFilter, pageable).map(OrderSummaryDto::fromDomain);
        }
        return orderRepository.findAll(pageable).map(OrderSummaryDto::fromDomain);
    }

    @Transactional(readOnly = true)
    public OrderDto getAdminOrderDetail(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .map(OrderDto::fromDomain)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with number: " + orderNumber));
    }

    @Transactional(readOnly = true)
    public OrderTimelineDto getAdminOrderTimeline(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with number: " + orderNumber));

        List<OrderStatusHistory> history = historyRepository.findByOrderId(order.getId());
        return OrderTimelineDto.fromDomain(order, history);
    }

    private void recordHistory(UUID orderId, OrderStatus prevStatus, OrderStatus newStatus, String reason, OrderActorType actorType, String actorId, String correlationId) {
        OrderStatusHistory history = OrderStatusHistory.recordTransition(
                orderId, prevStatus, newStatus, reason, actorType, actorId, correlationId
        );
        historyRepository.save(history);
    }

    private void releaseInventoryIfPresent(UUID orderId, String reason) {
        StockReservation reservation = reservationRepository.findByOrderId(orderId).orElse(null);
        if (reservation != null && reservation.isActive()) {
            inventoryApplicationService.releaseReservation(reservation.getId(), reason);
            log.info("Released stock reservation {} for order {} due to cancellation/expiration", reservation.getId(), orderId);
        }
    }
}
