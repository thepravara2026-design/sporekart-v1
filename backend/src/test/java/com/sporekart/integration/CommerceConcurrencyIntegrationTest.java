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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class CommerceConcurrencyIntegrationTest {

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
    @DisplayName("Concurrency: Last-item race condition (Inventory = 1). Exactly 1 customer succeeds, 1 fails, inventory stays >= 0")
    void testLastItemRaceCondition() throws Exception {
        String sku = "SKU-CONC-LAST-" + UUID.randomUUID().toString().substring(0, 6);
        ProductEntity p = CommerceFixtures.createTestProduct(productRepo, categoryRepo, sku, "Last Unit Prod", new BigDecimal("999.00"), ProductStatus.ACTIVE);
        CommerceFixtures.seedInventory(inventoryService, p.getId(), sku, 1); // Only 1 unit on hand!

        String custA = "cust-conc-A-" + UUID.randomUUID().toString().substring(0, 4);
        String custB = "cust-conc-B-" + UUID.randomUUID().toString().substring(0, 4);

        CommerceFixtures.createCartWithItem(cartService, custA, p.getId(), 1);
        CommerceFixtures.createCartWithItem(cartService, custB, p.getId(), 1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        executor.submit(() -> {
            try {
                startLatch.await();
                CommerceFixtures.CheckoutResult result = CommerceFixtures.executeCheckout(
                        orderService, inventoryService, paymentService, custA, "IDEM-CONC-A-" + UUID.randomUUID()
                );
                if (result.orderId() != null) {
                    successCount.incrementAndGet();
                }
            } catch (Exception e) {
                failureCount.incrementAndGet();
            } finally {
                finishLatch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                startLatch.await();
                CommerceFixtures.CheckoutResult result = CommerceFixtures.executeCheckout(
                        orderService, inventoryService, paymentService, custB, "IDEM-CONC-B-" + UUID.randomUUID()
                );
                if (result.orderId() != null) {
                    successCount.incrementAndGet();
                }
            } catch (Exception e) {
                failureCount.incrementAndGet();
            } finally {
                finishLatch.countDown();
            }
        });

        startLatch.countDown();
        finishLatch.await();
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(1);

        InventoryItemDto finalItem = inventoryService.getInventoryBySku(sku);
        assertThat(finalItem.onHandQuantity()).isEqualTo(1);
        assertThat(finalItem.reservedQuantity()).isEqualTo(1);
        assertThat(finalItem.availableQuantity()).isEqualTo(0);
    }
}
