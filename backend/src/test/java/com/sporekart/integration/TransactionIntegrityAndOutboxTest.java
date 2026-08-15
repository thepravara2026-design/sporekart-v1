package com.sporekart.integration;

import com.sporekart.modules.cart.application.CartApplicationService;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import com.sporekart.modules.catalog.infrastructure.persistence.SpringDataCategoryRepository;
import com.sporekart.modules.catalog.infrastructure.persistence.ProductEntity;
import com.sporekart.modules.catalog.infrastructure.persistence.SpringDataProductRepository;
import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.inventory.application.dto.InventoryItemDto;
import com.sporekart.modules.order.application.OrderApplicationService;
import com.sporekart.modules.order.infrastructure.persistence.SpringDataJpaOrderRepository;
import com.sporekart.modules.payment.application.PaymentApplicationService;
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
public class TransactionIntegrityAndOutboxTest {

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
    private SpringDataJpaOrderRepository orderRepo;

    @Test
    @DisplayName("Transactional Atomicity: Checkout completes Order creation and Inventory reservation in a single transaction")
    void testCheckoutTransactionalAtomicity() {
        String customerId = "cust-tx-" + UUID.randomUUID().toString().substring(0, 6);
        ProductEntity p = CommerceFixtures.createTestProduct(productRepo, categoryRepo, "SKU-TX-" + UUID.randomUUID().toString().substring(0, 4), "TX Prod", new BigDecimal("500.00"), ProductStatus.ACTIVE);
        CommerceFixtures.seedInventory(inventoryService, p.getId(), p.getSku(), 20);

        CommerceFixtures.createCartWithItem(cartService, customerId, p.getId(), 3);
        CommerceFixtures.CheckoutResult checkoutResult = CommerceFixtures.executeCheckout(
                orderService, inventoryService, paymentService, customerId, "IDEM-TX-" + UUID.randomUUID()
        );

        // Verify order persisted
        assertThat(orderRepo.findById(checkoutResult.orderId())).isPresent();

        // Verify inventory reserved in same atomic unit
        InventoryItemDto item = inventoryService.getInventoryBySku(p.getSku());
        assertThat(item.reservedQuantity()).isEqualTo(3);
        assertThat(item.onHandQuantity()).isEqualTo(20);
    }
}
