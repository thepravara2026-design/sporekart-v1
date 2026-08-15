package com.sporekart.modules.returns;

import com.sporekart.modules.order.domain.AddressSnapshot;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderItem;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.returns.domain.*;
import com.sporekart.modules.returns.domain.service.ReturnEligibilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReturnEligibilityServiceTest {

    private ReturnEligibilityService eligibilityService;
    private Order order;
    private OrderItem item1;

    @BeforeEach
    void setUp() {
        eligibilityService = new ReturnEligibilityService();
        UUID orderId = UUID.randomUUID();
        item1 = new OrderItem(
                UUID.randomUUID(),
                orderId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "SKU-PROD-1",
                "Test Product 1",
                "Default Variant",
                new BigDecimal("100.00"),
                5,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("500.00"),
                new BigDecimal("500.00"),
                OffsetDateTime.now()
        );

        List<OrderItem> items = new ArrayList<>();
        items.add(item1);

        AddressSnapshot address = new AddressSnapshot(
                "Jane Doe", "9876543210", "123 Main St", "Apt 4", "Metropolis", "NY", "10001", "USA"
        );

        order = new Order(
                orderId,
                "ORD-TEST-101",
                "cust-101",
                OrderStatus.DELIVERED,
                "INR",
                new BigDecimal("500.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("500.00"),
                "IDEM-101",
                address,
                "Standard Shipping",
                items,
                0L,
                OffsetDateTime.now().minusDays(2),
                OffsetDateTime.now().minusDays(2)
        );
    }

    @Test
    @DisplayName("Order within 14-day window should be ELIGIBLE")
    void testEligibleOrder() {
        ReturnEligibilityService.OrderEligibilityResult result = eligibilityService.evaluateEligibility(
                order, OffsetDateTime.now().minusDays(3), List.of()
        );

        assertTrue(result.eligible());
        assertEquals("ELIGIBLE", result.ineligibilityReason());
        assertEquals(1, result.itemEligibilities().size());
        assertEquals(5, result.itemEligibilities().get(0).returnableQuantity());
    }

    @Test
    @DisplayName("Order past 14-day window should be INELIGIBLE with RETURN_WINDOW_EXPIRED")
    void testExpiredReturnWindow() {
        ReturnEligibilityService.OrderEligibilityResult result = eligibilityService.evaluateEligibility(
                order, OffsetDateTime.now().minusDays(20), List.of()
        );

        assertFalse(result.eligible());
        assertEquals("RETURN_WINDOW_EXPIRED", result.ineligibilityReason());
    }

    @Test
    @DisplayName("Multi-return partial quantity accounting: remaining returnable quantity should decrease")
    void testPartialReturnAccounting() {
        Return existingReturn = Return.createNewRequest(
                "RET-PREV-1",
                order.getId(),
                order.getOrderNumber(),
                "cust-101",
                ReturnReasonCode.DAMAGED,
                "Damaged",
                null,
                "v1.0"
        );
        ReturnItem returnItem = ReturnItem.createNew(
                existingReturn.getId(),
                item1.getId(),
                item1.getProductId(),
                item1.getSku(),
                item1.getProductNameSnapshot(),
                2,
                item1.getUnitPrice(),
                ReturnReasonCode.DAMAGED
        );
        existingReturn.addItem(returnItem);
        existingReturn.approve("admin-1", "Approved", "corr-1");

        ReturnEligibilityService.OrderEligibilityResult result = eligibilityService.evaluateEligibility(
                order, OffsetDateTime.now().minusDays(2), List.of(existingReturn)
        );

        assertTrue(result.eligible());
        assertEquals(1, result.itemEligibilities().size());
        assertEquals(2, result.itemEligibilities().get(0).previouslyReturnedQuantity());
        assertEquals(3, result.itemEligibilities().get(0).returnableQuantity());
    }
}
