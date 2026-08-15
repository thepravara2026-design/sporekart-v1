package com.sporekart.modules.returns;

import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.order.domain.AddressSnapshot;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderItem;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.infrastructure.persistence.OrderRepository;
import com.sporekart.modules.payment.domain.Payment;
import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.payment.infrastructure.persistence.PaymentRepository;
import com.sporekart.modules.returns.application.ReturnApplicationService;
import com.sporekart.modules.returns.application.dto.*;
import com.sporekart.modules.returns.domain.InspectionOutcome;
import com.sporekart.modules.returns.domain.ReturnReasonCode;
import com.sporekart.modules.returns.domain.ReturnStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReturnFullLifecycleHandoffTest {

    @Autowired
    private ReturnApplicationService returnApplicationService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private InventoryApplicationService inventoryApplicationService;

    @Autowired
    private com.sporekart.modules.returns.domain.ReturnRepository returnRepository;

    @Test
    @Transactional
    @DisplayName("E2E Return Journey: Order Delivered -> Eligibility -> Request -> Approval -> Receipt -> Inspection -> Refund -> Stock Restored")
    void testFullReturnLifecycle() {
        UUID orderId = UUID.randomUUID();
        String orderRef = "ORD-E2E-" + UUID.randomUUID().toString().substring(0, 6);
        String customerId = "cust-e2e-101";

        UUID orderItemId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        String sku = "SKU-E2E-R1";

        OrderItem item = new OrderItem(
                orderItemId,
                orderId,
                productId,
                UUID.randomUUID(),
                sku,
                "E2E Returnable Product",
                "Default Variant",
                new BigDecimal("150.00"),
                2,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("300.00"),
                new BigDecimal("300.00"),
                OffsetDateTime.now()
        );
        List<OrderItem> items = new ArrayList<>();
        items.add(item);

        AddressSnapshot address = new AddressSnapshot(
                "Jane Doe", "9876543210", "123 Main St", "Apt 4", "Metropolis", "NY", "10001", "USA"
        );

        Order order = new Order(
                orderId,
                orderRef,
                customerId,
                OrderStatus.DELIVERED,
                "INR",
                new BigDecimal("300.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("300.00"),
                "IDEM-E2E-1",
                address,
                "Express",
                items,
                0L,
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().minusDays(1)
        );
        orderRepository.save(order);

        // Create Payment
        Payment payment = Payment.createNewPayment("PAY-" + orderRef, orderId, customerId, new BigDecimal("300.00"), "INR", PaymentProviderType.MOCK);
        com.sporekart.modules.payment.domain.PaymentAttempt attempt = payment.createAttempt("ATT-" + orderRef);
        payment.markSuccess(attempt.getId(), "pay_mock_e2e", "sig_mock");
        payment = paymentRepository.save(payment);

        // 1. Check Return Eligibility
        ReturnEligibilityDto eligibility = returnApplicationService.checkEligibility(orderRef, customerId);
        assertTrue(eligibility.eligible());
        assertEquals(1, eligibility.items().size());
        assertEquals(2, eligibility.items().get(0).returnableQuantity());

        // 2. Submit Return Request
        CreateReturnRequestDto req = new CreateReturnRequestDto(
                ReturnReasonCode.DEFECTIVE,
                "Item screen flickers",
                "http://evidence.link/img1.jpg",
                List.of(new CreateReturnRequestDto.CreateReturnItemInput(orderItemId, 2, ReturnReasonCode.DEFECTIVE))
        );

        ReturnDto returnDto = returnApplicationService.createReturn(orderRef, req, customerId);
        assertNotNull(returnDto.returnReference());
        assertEquals(ReturnStatus.REQUESTED, returnDto.status());
        assertEquals(new BigDecimal("300.00"), returnDto.totalRefundableAmount());

        // 3. Admin Approves Return
        ReturnDto approved = returnApplicationService.approveReturn(returnDto.returnReference(), "admin-1", "Approved for return");
        assertEquals(ReturnStatus.APPROVED, approved.status());

        // 4. Record Warehouse Receipt & Inspection
        ReturnInspectionDto inspectionDto = new ReturnInspectionDto(null, "inspector-1", InspectionOutcome.ACCEPTED, "Passed warehouse QA", null, List.of());

        // Mark received first
        com.sporekart.modules.returns.domain.Return returnAgg = returnRepository.findByReturnReference(returnDto.returnReference()).orElseThrow();
        returnAgg.markReceived("corr-rec-1");
        returnRepository.save(returnAgg);

        ReturnDto refundedReturn = returnApplicationService.processInspection(returnDto.returnReference(), inspectionDto, "inspector-1");
        assertEquals(ReturnStatus.REFUNDED, refundedReturn.status());
        assertNotNull(refundedReturn.refundRecord());
        assertEquals("PROCESSED", refundedReturn.refundRecord().status());
        assertEquals(new BigDecimal("300.00"), refundedReturn.refundRecord().amount());
    }
}
