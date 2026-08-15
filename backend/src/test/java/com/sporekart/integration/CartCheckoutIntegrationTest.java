package com.sporekart.integration;

import com.sporekart.modules.cart.application.CartApplicationService;
import com.sporekart.modules.cart.application.dto.AddCartItemCommand;
import com.sporekart.modules.cart.application.dto.CartDto;
import com.sporekart.modules.cart.application.dto.UpdateCartItemCommand;
import com.sporekart.modules.cart.domain.exception.CartNotFoundException;
import com.sporekart.modules.cart.infrastructure.persistence.CartRepository;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import com.sporekart.modules.catalog.infrastructure.persistence.SpringDataCategoryRepository;
import com.sporekart.modules.catalog.infrastructure.persistence.ProductEntity;
import com.sporekart.modules.catalog.infrastructure.persistence.SpringDataProductRepository;
import com.sporekart.modules.checkout.application.CheckoutApplicationService;
import com.sporekart.modules.checkout.application.dto.CheckoutPreviewRequest;
import com.sporekart.modules.checkout.application.dto.CheckoutPreviewResponse;
import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.order.application.OrderApplicationService;
import com.sporekart.modules.order.application.dto.CreateOrderCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CartCheckoutIntegrationTest {

    @Autowired
    private SpringDataProductRepository productRepo;

    @Autowired
    private SpringDataCategoryRepository categoryRepo;

    @Autowired
    private CartApplicationService cartService;

    @Autowired
    private CheckoutApplicationService checkoutService;

    @Autowired
    private InventoryApplicationService inventoryService;

    @Autowired
    private OrderApplicationService orderService;

    @Autowired
    private CartRepository cartRepository;

    @Test
    @DisplayName("Cart item add, update, calculate subtotal and server-side authoritative pricing")
    void testCartItemAdditionAndUpdate() {
        String customerId = "cust-cart-" + UUID.randomUUID().toString().substring(0, 6);
        ProductEntity p1 = CommerceFixtures.createTestProduct(productRepo, categoryRepo, "SKU-C1-" + UUID.randomUUID().toString().substring(0, 4), "Prod 1", new BigDecimal("100.00"), ProductStatus.ACTIVE);
        ProductEntity p2 = CommerceFixtures.createTestProduct(productRepo, categoryRepo, "SKU-C2-" + UUID.randomUUID().toString().substring(0, 4), "Prod 2", new BigDecimal("250.00"), ProductStatus.ACTIVE);

        CommerceFixtures.seedInventory(inventoryService, p1.getId(), p1.getSku(), 50);
        CommerceFixtures.seedInventory(inventoryService, p2.getId(), p2.getSku(), 50);

        CartDto cart = cartService.addItemToCart(customerId, new AddCartItemCommand(p1.getId(), null, 2));
        cart = cartService.addItemToCart(customerId, new AddCartItemCommand(p2.getId(), null, 1));

        assertThat(cart.items()).hasSize(2);
        assertThat(cart.subtotal()).isEqualByComparingTo(new BigDecimal("450.00"));

        // Update quantity
        UUID itemId = cart.items().stream().filter(i -> i.productId().equals(p1.getId())).findFirst().orElseThrow().id();
        cart = cartService.updateCartItemQuantity(customerId, itemId, new UpdateCartItemCommand(5));
        assertThat(cart.subtotal()).isEqualByComparingTo(new BigDecimal("750.00")); // (5*100) + (1*250)

        // Checkout Preview Authoritative Pricing
        CheckoutPreviewResponse preview = checkoutService.generateCheckoutPreview(customerId, new CheckoutPreviewRequest("Bengaluru", null));
        assertThat(preview.breakdown().subtotal()).isEqualByComparingTo(new BigDecimal("750.00"));
    }

    @Test
    @DisplayName("Price change race condition: checkout preserves server-side authoritative catalog price")
    void testPriceChangeRaceCondition() {
        String customerId = "cust-race-" + UUID.randomUUID().toString().substring(0, 6);
        ProductEntity p = CommerceFixtures.createTestProduct(productRepo, categoryRepo, "SKU-RACE-" + UUID.randomUUID().toString().substring(0, 4), "Race Prod", new BigDecimal("100.00"), ProductStatus.ACTIVE);
        CommerceFixtures.seedInventory(inventoryService, p.getId(), p.getSku(), 50);

        CartDto cart = cartService.addItemToCart(customerId, new AddCartItemCommand(p.getId(), null, 1));
        assertThat(cart.subtotal()).isEqualByComparingTo(new BigDecimal("100.00"));

        // Price changes in catalog to 120.00 after adding to cart
        ProductEntity updatedProduct = new ProductEntity(
                p.getId(), p.getSku(), p.getName(), p.getDescription(), new BigDecimal("120.00"), p.getCurrency(), p.getStatus(), p.getCategory(), p.getCreatedAt(), Instant.now()
        );
        productRepo.save(updatedProduct);

        // Checkout preview recalculates with current authoritative catalog price
        CheckoutPreviewResponse preview = checkoutService.generateCheckoutPreview(customerId, new CheckoutPreviewRequest("Bengaluru", null));
        assertThat(preview.breakdown().subtotal()).isEqualByComparingTo(new BigDecimal("120.00"));
    }

    @Test
    @DisplayName("Empty cart checkout attempt should fail cleanly with CartNotFoundException")
    void testEmptyCartCheckout() {
        String customerId = "cust-empty-" + UUID.randomUUID().toString().substring(0, 6);

        CreateOrderCommand createCmd = new CreateOrderCommand(CommerceFixtures.createTestAddress(), "IDEM-EMPTY-" + UUID.randomUUID(), "Empty cart notes");

        assertThatThrownBy(() -> orderService.createOrder(customerId, createCmd))
                .isInstanceOf(CartNotFoundException.class);
    }

    @Test
    @DisplayName("Cart isolation: Customer A cannot checkout Customer B's order (IDOR protection)")
    void testCartIsolationIDOR() {
        String custA = "cust-A-" + UUID.randomUUID().toString().substring(0, 4);
        String custB = "cust-B-" + UUID.randomUUID().toString().substring(0, 4);

        ProductEntity p = CommerceFixtures.createTestProduct(productRepo, categoryRepo, "SKU-ISO-" + UUID.randomUUID().toString().substring(0, 4), "Iso Prod", new BigDecimal("50.00"), ProductStatus.ACTIVE);
        CommerceFixtures.seedInventory(inventoryService, p.getId(), p.getSku(), 50);

        cartService.addItemToCart(custA, new AddCartItemCommand(p.getId(), null, 1));
        CreateOrderCommand createCmd = new CreateOrderCommand(CommerceFixtures.createTestAddress(), "IDEM-ISO-" + UUID.randomUUID(), "Notes");
        var orderA = orderService.createOrder(custA, createCmd);

        // Customer B attempts to view Customer A's order by ID
        assertThatThrownBy(() -> orderService.getOrderDetail(custB, orderA.id()))
                .isInstanceOf(RuntimeException.class);
    }
}
