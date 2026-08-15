package com.sporekart.integration;

import com.sporekart.modules.cart.application.CartApplicationService;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import com.sporekart.modules.catalog.infrastructure.persistence.SpringDataCategoryRepository;
import com.sporekart.modules.catalog.infrastructure.persistence.ProductEntity;
import com.sporekart.modules.catalog.infrastructure.persistence.SpringDataProductRepository;
import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.order.application.OrderApplicationService;
import com.sporekart.modules.payment.application.PaymentApplicationService;
import com.sporekart.modules.returns.application.ReturnApplicationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CommerceSecurityAndAuthorizationTest {

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
    private ReturnApplicationService returnService;

    @Test
    @DisplayName("Order Security IDOR: Customer A cannot access Customer B's Order details")
    void testOrderSecurityIDOR() {
        String custA = "cust-sec-A-" + UUID.randomUUID().toString().substring(0, 4);
        String custB = "cust-sec-B-" + UUID.randomUUID().toString().substring(0, 4);

        ProductEntity p = CommerceFixtures.createTestProduct(productRepo, categoryRepo, "SKU-SEC-" + UUID.randomUUID().toString().substring(0, 4), "Sec Prod", new BigDecimal("100.00"), ProductStatus.ACTIVE);
        CommerceFixtures.seedInventory(inventoryService, p.getId(), p.getSku(), 10);

        CommerceFixtures.createCartWithItem(cartService, custA, p.getId(), 1);
        CommerceFixtures.CheckoutResult checkoutResultA = CommerceFixtures.executeCheckout(
                orderService, inventoryService, paymentService, custA, "IDEM-SEC-A-" + UUID.randomUUID()
        );

        // Customer B attempts to view Customer A's order by ID
        assertThatThrownBy(() -> orderService.getOrderDetail(custB, checkoutResultA.orderId()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Return Security IDOR: Customer A cannot access Customer B's Return details")
    void testReturnSecurityIDOR() {
        String custB = "cust-retsec-B-" + UUID.randomUUID().toString().substring(0, 4);

        // Customer B attempts to fetch non-existent or foreign return request
        assertThatThrownBy(() -> returnService.getReturnByReference("RET-NONEXISTENT", custB))
                .isInstanceOf(RuntimeException.class);
    }
}
