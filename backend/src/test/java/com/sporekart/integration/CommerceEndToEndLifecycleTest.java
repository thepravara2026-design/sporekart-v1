package com.sporekart.integration;

import com.sporekart.modules.cart.application.CartApplicationService;
import com.sporekart.modules.cart.application.dto.CartDto;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import com.sporekart.modules.catalog.infrastructure.persistence.SpringDataCategoryRepository;
import com.sporekart.modules.catalog.infrastructure.persistence.ProductEntity;
import com.sporekart.modules.catalog.infrastructure.persistence.SpringDataProductRepository;
import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.inventory.application.dto.InventoryItemDto;
import com.sporekart.modules.order.application.OrderApplicationService;
import com.sporekart.modules.order.application.dto.OrderDto;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.payment.application.PaymentApplicationService;
import com.sporekart.modules.payment.application.dto.PaymentDto;
import com.sporekart.modules.payment.application.dto.PaymentVerificationCommand;
import com.sporekart.modules.returns.application.ReturnApplicationService;
import com.sporekart.modules.returns.application.dto.CreateReturnRequestDto;
import com.sporekart.modules.returns.application.dto.ReturnDto;
import com.sporekart.modules.returns.application.dto.ReturnInspectionDto;
import com.sporekart.modules.returns.domain.InspectionOutcome;
import com.sporekart.modules.returns.domain.ReturnReasonCode;
import com.sporekart.modules.shipment.application.ShipmentApplicationService;
import com.sporekart.modules.shipment.application.dto.ShipmentDto;
import com.sporekart.modules.shipment.domain.ShipmentProviderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CommerceEndToEndLifecycleTest {

    @Autowired
    private SpringDataProductRepository productRepo;

    @Autowired
    private SpringDataCategoryRepository categoryRepo;

    @Autowired
    private CartApplicationService cartService;

    @Autowired
    private OrderApplicationService orderService;

    @Autowired
    private InventoryApplicationService inventoryService;

    @Autowired
    private PaymentApplicationService paymentService;

    @Autowired
    private ShipmentApplicationService shipmentService;

    @Autowired
    private ReturnApplicationService returnService;

    @Test
    @DisplayName("Complete E2E Commerce Journey: Browse -> Cart -> Checkout -> Order -> Inventory -> Payment -> Shipping -> Delivery -> Return -> Refund")
    void testCompleteCommerceLifecycle() {
        String customerId = "cust-e2e-" + UUID.randomUUID().toString().substring(0, 8);
        String sku = "SKU-E2E-" + UUID.randomUUID().toString().substring(0, 6);

        // 1. Seed Product & Inventory
        ProductEntity product = CommerceFixtures.createTestProduct(productRepo, categoryRepo, sku, "Premium Organic Mushroom", new BigDecimal("499.00"), ProductStatus.ACTIVE);
        InventoryItemDto inventory = CommerceFixtures.seedInventory(inventoryService, product.getId(), sku, 100);

        assertThat(inventory.onHandQuantity()).isEqualTo(100);
        assertThat(inventory.reservedQuantity()).isEqualTo(0);

        // 2. Add to Cart
        CartDto cart = CommerceFixtures.createCartWithItem(cartService, customerId, product.getId(), 2);
        assertThat(cart.items()).hasSize(1);
        assertThat(cart.subtotal()).isEqualByComparingTo(new BigDecimal("998.00"));

        // 3. Checkout (Order Creation + Inventory Reservation + Payment Creation)
        String idempotencyKey = "IDEM-E2E-" + UUID.randomUUID();
        CommerceFixtures.CheckoutResult checkoutResult = CommerceFixtures.executeCheckout(
                orderService, inventoryService, paymentService, customerId, idempotencyKey
        );

        assertThat(checkoutResult.orderId()).isNotNull();
        assertThat(checkoutResult.orderNumber()).isNotNull();
        assertThat(checkoutResult.paymentId()).isNotNull();

        // 4. Verify Order Created & Inventory Reserved
        OrderDto order = orderService.getOrderDetail(customerId, checkoutResult.orderId());
        assertThat(order.status()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.grandTotal()).isGreaterThanOrEqualTo(new BigDecimal("998.00"));

        InventoryItemDto updatedInventory = inventoryService.getInventoryBySku(sku);
        assertThat(updatedInventory.onHandQuantity()).isEqualTo(100);
        assertThat(updatedInventory.reservedQuantity()).isEqualTo(2);

        // 5. Payment Verification (Success -> Order Confirmed & Paid)
        PaymentVerificationCommand verifyCmd = new PaymentVerificationCommand(
                checkoutResult.paymentReference(),
                checkoutResult.providerOrderId(),
                "pay_mock_" + UUID.randomUUID().toString().substring(0, 8),
                "mock_signature"
        );
        PaymentDto payment = paymentService.verifyPayment(verifyCmd, customerId);
        assertThat(payment.status().name()).isEqualTo("SUCCESS");

        OrderDto confirmedOrder = orderService.getOrderDetail(customerId, checkoutResult.orderId());
        assertThat(confirmedOrder.status()).isIn(OrderStatus.CONFIRMED, OrderStatus.PAID, OrderStatus.PROCESSING, OrderStatus.READY_FOR_FULFILMENT);

        // 6. Shipping & Fulfilment Orchestration
        ShipmentDto shipment = shipmentService.createShipmentForOrder(checkoutResult.orderId());
        assertThat(shipment.shipmentReference()).isNotNull();

        // Step-by-step order lifecycle progression for return eligibility
        orderService.startProcessing(checkoutResult.orderId(), "system");
        orderService.markReadyForFulfilment(checkoutResult.orderId(), "system");
        orderService.markShipped(checkoutResult.orderId(), "system", shipment.awb());
        orderService.markOutForDelivery(checkoutResult.orderId(), "system");
        OrderDto deliveredOrder = orderService.markDelivered(checkoutResult.orderId(), "system");

        assertThat(deliveredOrder.status()).isEqualTo(OrderStatus.DELIVERED);

        // 7. Customer Returns & Refund Orchestration
        CreateReturnRequestDto returnReq = new CreateReturnRequestDto(
                ReturnReasonCode.DAMAGED,
                "Damaged box on arrival",
                "http://example.com/evidence.jpg",
                List.of(new CreateReturnRequestDto.CreateReturnItemInput(
                        deliveredOrder.items().get(0).id(),
                        1,
                        ReturnReasonCode.DAMAGED
                ))
        );

        ReturnDto returnDto = returnService.createReturn(deliveredOrder.orderNumber(), returnReq, customerId);
        assertThat(returnDto.status().name()).isEqualTo("REQUESTED");

        ReturnDto approvedReturn = returnService.approveReturn(returnDto.returnReference(), "admin-1", "Approved for return");
        assertThat(approvedReturn.status().name()).isEqualTo("APPROVED");

        ReturnInspectionDto inspectionDto = new ReturnInspectionDto(
                null,
                "inspector-1",
                InspectionOutcome.ACCEPTED,
                "Inspected and accepted for refund",
                OffsetDateTime.now(),
                List.of()
        );

        ReturnDto inspectedReturn = returnService.processInspection(approvedReturn.returnReference(), inspectionDto, "inspector-1");
        assertThat(inspectedReturn.status().name()).isEqualTo("REFUNDED");
        assertThat(inspectedReturn.refundRecord()).isNotNull();
        assertThat(inspectedReturn.refundRecord().status()).isEqualTo("PROCESSED");
        assertThat(inspectedReturn.refundRecord().amount()).isEqualByComparingTo(new BigDecimal("499.00"));
    }
}
