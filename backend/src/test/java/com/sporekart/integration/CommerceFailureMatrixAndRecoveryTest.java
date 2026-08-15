package com.sporekart.integration;

import com.sporekart.modules.cart.application.CartApplicationService;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import com.sporekart.modules.catalog.infrastructure.persistence.SpringDataCategoryRepository;
import com.sporekart.modules.catalog.infrastructure.persistence.ProductEntity;
import com.sporekart.modules.catalog.infrastructure.persistence.SpringDataProductRepository;
import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.inventory.application.dto.InventoryItemDto;
import com.sporekart.modules.order.application.OrderApplicationService;
import com.sporekart.modules.payment.application.PaymentApplicationService;
import com.sporekart.modules.payment.domain.PaymentProviderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CommerceFailureMatrixAndRecoveryTest {

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

    @Test
    @DisplayName("Failure Matrix test: Payment failure compensation releases inventory and updates order status")
    void testFailureMatrixPaymentDeclineRecovery() {
        String customerId = "cust-fail-matrix-" + UUID.randomUUID().toString().substring(0, 6);
        ProductEntity p = CommerceFixtures.createTestProduct(productRepo, categoryRepo, "SKU-FM-" + UUID.randomUUID().toString().substring(0, 4), "Fail Matrix Prod", new BigDecimal("600.00"), ProductStatus.ACTIVE);
        CommerceFixtures.seedInventory(inventoryService, p.getId(), p.getSku(), 15);

        CommerceFixtures.createCartWithItem(cartService, customerId, p.getId(), 2);
        CommerceFixtures.CheckoutResult checkoutResult = CommerceFixtures.executeCheckout(
                orderService, inventoryService, paymentService, customerId, "IDEM-FM-" + UUID.randomUUID()
        );

        // Verify inventory reserved
        InventoryItemDto itemBefore = inventoryService.getInventoryBySku(p.getSku());
        assertThat(itemBefore.reservedQuantity()).isEqualTo(2);

        // Payment webhook failure event from provider
        String rawWebhookBody = "{\"event\":\"payment.failed\",\"event_id\":\"evt_fail_123\",\"payload\":{\"payment\":{\"entity\":{\"order_id\":\"" + checkoutResult.providerOrderId() + "\"}}}}";
        paymentService.processWebhook(PaymentProviderType.MOCK, rawWebhookBody, "mock_signature");

        // Verify compensation: reserved inventory released back to 0
        InventoryItemDto itemAfter = inventoryService.getInventoryBySku(p.getSku());
        assertThat(itemAfter.reservedQuantity()).isEqualTo(0);
        assertThat(itemAfter.availableQuantity()).isEqualTo(15);
    }
}
