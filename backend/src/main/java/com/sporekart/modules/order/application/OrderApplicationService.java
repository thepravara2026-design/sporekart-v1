package com.sporekart.modules.order.application;

import com.sporekart.modules.cart.domain.Cart;
import com.sporekart.modules.cart.domain.CartStatus;
import com.sporekart.modules.cart.domain.exception.CartNotFoundException;
import com.sporekart.modules.cart.infrastructure.persistence.CartRepository;
import com.sporekart.modules.checkout.application.CheckoutPricingService;
import com.sporekart.modules.checkout.domain.exception.CartEmptyException;
import com.sporekart.modules.checkout.domain.model.CheckoutLineItem;
import com.sporekart.modules.checkout.domain.model.CheckoutPreview;
import com.sporekart.modules.order.application.dto.CreateOrderCommand;
import com.sporekart.modules.order.application.dto.OrderDto;
import com.sporekart.modules.order.application.dto.OrderSummaryDto;
import com.sporekart.modules.order.domain.AddressSnapshot;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderItem;
import com.sporekart.modules.order.domain.OrderNumberPort;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.domain.exception.OrderAccessDeniedException;
import com.sporekart.modules.order.domain.exception.OrderNotFoundException;
import com.sporekart.modules.order.infrastructure.persistence.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final CartRepository cartRepository;
    private final CheckoutPricingService checkoutPricingService;
    private final OrderNumberPort orderNumberPort;

    public OrderApplicationService(
            OrderRepository orderRepository,
            CartRepository cartRepository,
            CheckoutPricingService checkoutPricingService,
            OrderNumberPort orderNumberPort
    ) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.checkoutPricingService = checkoutPricingService;
        this.orderNumberPort = orderNumberPort;
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
                    null, // variantId if extended in future
                    line.getSku(),
                    line.getProductName(),
                    null, // variantName if extended in future
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
                now,
                now
        );

        // 7. Persist order
        Order savedOrder = orderRepository.save(order);

        // 8. Transition cart to CHECKED_OUT
        activeCart.markAsCheckedOut();
        cartRepository.save(activeCart);

        log.info("Successfully created order {} (number: {}) for customer {}", savedOrder.getId(), savedOrder.getOrderNumber(), customerId);
        return OrderDto.fromDomain(savedOrder);
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

    @Transactional
    public OrderDto cancelOrder(String customerId, UUID orderId) {
        Order order = orderRepository.findByIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.cancel();
        Order updated = orderRepository.save(order);
        log.info("Cancelled order {} for customer {}", orderId, customerId);
        return OrderDto.fromDomain(updated);
    }
}
