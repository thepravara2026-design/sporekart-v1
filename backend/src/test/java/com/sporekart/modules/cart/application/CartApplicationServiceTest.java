package com.sporekart.modules.cart.application;

import com.sporekart.modules.cart.application.dto.AddCartItemCommand;
import com.sporekart.modules.cart.application.dto.CartDto;
import com.sporekart.modules.cart.application.dto.UpdateCartItemCommand;
import com.sporekart.modules.cart.domain.Cart;
import com.sporekart.modules.cart.domain.CartStatus;
import com.sporekart.modules.cart.domain.exception.CartItemNotFoundException;
import com.sporekart.modules.cart.domain.exception.ProductNotPurchasableException;
import com.sporekart.modules.cart.domain.port.CatalogPort;
import com.sporekart.modules.cart.infrastructure.config.CartProperties;
import com.sporekart.modules.cart.infrastructure.persistence.CartRepository;
import com.sporekart.modules.catalog.domain.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CartApplicationServiceTest {

    private CartRepository cartRepository;
    private CatalogPort catalogPort;
    private CartProperties cartProperties;
    private CartApplicationService cartService;

    private final String customerId = "cust-200";
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        cartRepository = mock(CartRepository.class);
        catalogPort = mock(CatalogPort.class);
        cartProperties = new CartProperties();
        cartService = new CartApplicationService(cartRepository, catalogPort, cartProperties);
    }

    @Test
    @DisplayName("Should create active cart if customer does not have one")
    void shouldCreateCartIfNotExist() {
        when(cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        CartDto dto = cartService.getOrCreateActiveCart(customerId);

        assertNotNull(dto);
        assertEquals(customerId, dto.customerId());
        assertEquals("ACTIVE", dto.status());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should add item to active cart after validating catalog product")
    void shouldAddItemToCart() {
        Cart cart = Cart.createNewActiveCart(customerId, "INR");
        when(cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        CatalogPort.CatalogProductDetails productDetails = new CatalogPort.CatalogProductDetails(
                productId, "Lion's Mane Spore Culture", "LM-001", new BigDecimal("35.00"), "INR", true, "ACTIVE"
        );
        when(catalogPort.getProductForCart(productId)).thenReturn(productDetails);

        AddCartItemCommand command = new AddCartItemCommand(productId, null, 2);
        CartDto dto = cartService.addItemToCart(customerId, command);

        assertNotNull(dto);
        assertEquals(1, dto.items().size());
        assertEquals(2, dto.itemCount());
        assertEquals(new BigDecimal("70.00"), dto.subtotal());
        verify(catalogPort, times(1)).getProductForCart(productId);
    }

    @Test
    @DisplayName("Should throw exception when adding non-existent catalog product")
    void shouldThrowExceptionWhenProductNotFound() {
        Cart cart = Cart.createNewActiveCart(customerId, "INR");
        when(cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
        when(catalogPort.getProductForCart(productId)).thenThrow(new ProductNotFoundException(productId));

        AddCartItemCommand command = new AddCartItemCommand(productId, null, 1);
        assertThrows(ProductNotFoundException.class, () -> cartService.addItemToCart(customerId, command));
    }

    @Test
    @DisplayName("Should throw exception when adding inactive catalog product")
    void shouldThrowExceptionWhenProductInactive() {
        Cart cart = Cart.createNewActiveCart(customerId, "INR");
        when(cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
        when(catalogPort.getProductForCart(productId)).thenThrow(new ProductNotPurchasableException(productId, "Product status is DRAFT"));

        AddCartItemCommand command = new AddCartItemCommand(productId, null, 1);
        assertThrows(ProductNotPurchasableException.class, () -> cartService.addItemToCart(customerId, command));
    }

    @Test
    @DisplayName("Should update cart item quantity successfully")
    void shouldUpdateCartItemQuantity() {
        Cart cart = Cart.createNewActiveCart(customerId, "INR");
        var item = cart.addItem(productId, null, "SKU-1", "Product", null, new BigDecimal("10.00"), 1, 50, 100);
        when(cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateCartItemCommand command = new UpdateCartItemCommand(5);
        CartDto dto = cartService.updateCartItemQuantity(customerId, item.getId(), command);

        assertEquals(5, dto.itemCount());
        assertEquals(new BigDecimal("50.00"), dto.subtotal());
    }

    @Test
    @DisplayName("Should remove item and clear cart successfully")
    void shouldRemoveAndClearCart() {
        Cart cart = Cart.createNewActiveCart(customerId, "INR");
        var item = cart.addItem(productId, null, "SKU-1", "Product", null, new BigDecimal("10.00"), 2, 50, 100);
        when(cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        CartDto afterRemove = cartService.removeCartItem(customerId, item.getId());
        assertEquals(0, afterRemove.itemCount());

        CartDto afterClear = cartService.clearCart(customerId);
        assertEquals(0, afterClear.itemCount());
    }
}
