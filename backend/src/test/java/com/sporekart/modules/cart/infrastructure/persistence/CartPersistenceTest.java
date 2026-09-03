package com.sporekart.modules.cart.infrastructure.persistence;

import com.sporekart.modules.cart.domain.Cart;
import com.sporekart.modules.cart.domain.CartItem;
import com.sporekart.modules.cart.domain.CartStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CartPersistenceTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private SpringDataJpaCartRepository jpaCartRepository;

    @Test
    @Transactional
    @DisplayName("Should persist and retrieve active cart with items")
    void shouldPersistAndRetrieveCart() {
        String customerId = "cust-pers-1";
        Cart cart = Cart.createNewActiveCart(customerId, "INR");
        UUID productId = UUID.randomUUID();
        cart.addItem(productId, null, "SKU-PERS-1", "Persistent Product", null, new BigDecimal("15.50"), 3, 50, 100);

        Cart saved = cartRepository.save(cart);
        assertNotNull(saved.getId());

        Optional<Cart> retrievedOpt = cartRepository.findById(saved.getId());
        assertTrue(retrievedOpt.isPresent());

        Cart retrieved = retrievedOpt.get();
        assertEquals(customerId, retrieved.getCustomerId());
        assertEquals(CartStatus.ACTIVE, retrieved.getStatus());
        assertEquals(1, retrieved.getItems().size());
        assertEquals(3, retrieved.getItemCount());
        assertEquals(new BigDecimal("46.50"), retrieved.getSubtotal());

        CartItem item = retrieved.getItems().get(0);
        assertEquals(productId, item.getProductId());
        assertEquals("SKU-PERS-1", item.getSku());
        assertEquals("Persistent Product", item.getProductNameSnapshot());
        assertEquals(new BigDecimal("15.50"), item.getUnitPriceSnapshot());
        assertEquals(3, item.getQuantity());
        assertEquals(new BigDecimal("46.50"), item.getLineTotal());
    }

    @Test
    @Transactional
    @DisplayName("Should enforce unique active cart constraint per customer in database")
    void shouldEnforceUniqueActiveCartConstraint() {
        String customerId = "cust-unique-1";
        Cart cart1 = Cart.createNewActiveCart(customerId, "INR");
        cartRepository.save(cart1);

        Cart cart2 = Cart.createNewActiveCart(customerId, "INR");
        assertThrows(DataIntegrityViolationException.class, () -> {
            cartRepository.save(cart2);
            jpaCartRepository.flush();
        });
    }

    @Test
    @Transactional
    @DisplayName("Should cascade delete line items when cart is deleted")
    void shouldCascadeDeleteLineItems() {
        String customerId = "cust-cascade-1";
        Cart cart = Cart.createNewActiveCart(customerId, "INR");
        cart.addItem(UUID.randomUUID(), null, "SKU-C1", "Product 1", null, new BigDecimal("10.00"), 1, 50, 100);
        Cart saved = cartRepository.save(cart);

        cartRepository.deleteById(saved.getId());
        jpaCartRepository.flush();

        Optional<Cart> retrieved = cartRepository.findById(saved.getId());
        assertTrue(retrieved.isEmpty());
    }
}
