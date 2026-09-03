package com.sporekart.modules.cart.application;

import com.sporekart.modules.cart.application.dto.AddCartItemCommand;
import com.sporekart.modules.cart.application.dto.CartDto;
import com.sporekart.modules.cart.application.dto.UpdateCartItemCommand;
import com.sporekart.modules.cart.domain.Cart;
import com.sporekart.modules.cart.domain.CartStatus;
import com.sporekart.modules.cart.domain.exception.CartNotFoundException;
import com.sporekart.modules.cart.domain.port.CatalogPort;
import com.sporekart.modules.cart.infrastructure.config.CartProperties;
import com.sporekart.modules.cart.infrastructure.persistence.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CartApplicationService {

    private static final Logger log = LoggerFactory.getLogger(CartApplicationService.class);

    private final CartRepository cartRepository;
    private final CatalogPort catalogPort;
    private final CartProperties cartProperties;

    public CartApplicationService(
            CartRepository cartRepository,
            CatalogPort catalogPort,
            CartProperties cartProperties
    ) {
        this.cartRepository = cartRepository;
        this.catalogPort = catalogPort;
        this.cartProperties = cartProperties;
    }

    @Transactional
    public CartDto getOrCreateActiveCart(String customerId) {
        Cart cart = getOrCreateActiveCartDomain(customerId);
        return CartDto.fromDomain(cart);
    }

    @Transactional
    public CartDto addItemToCart(String customerId, AddCartItemCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("AddCartItemCommand cannot be null");
        }

        Cart cart = getOrCreateActiveCartDomain(customerId);
        CatalogPort.CatalogProductDetails productDetails = catalogPort.getProductForCart(command.productId());

        cart.addItem(
                productDetails.id(),
                command.variantId(),
                productDetails.sku(),
                productDetails.name(),
                null,
                productDetails.price(),
                command.quantity(),
                cartProperties.getMaxItemQuantity(),
                cartProperties.getMaxTotalItems()
        );

        Cart saved = cartRepository.save(cart);
        log.info("Added product {} (qty={}) to cart {} for customer {}", command.productId(), command.quantity(), saved.getId(), customerId);
        return CartDto.fromDomain(saved);
    }

    @Transactional
    public CartDto updateCartItemQuantity(String customerId, UUID itemId, UpdateCartItemCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("UpdateCartItemCommand cannot be null");
        }

        Cart cart = getActiveCartDomain(customerId);
        cart.updateItemQuantity(
                itemId,
                command.quantity(),
                cartProperties.getMaxItemQuantity(),
                cartProperties.getMaxTotalItems()
        );

        Cart saved = cartRepository.save(cart);
        log.info("Updated item {} quantity to {} in cart {} for customer {}", itemId, command.quantity(), saved.getId(), customerId);
        return CartDto.fromDomain(saved);
    }

    @Transactional
    public CartDto removeCartItem(String customerId, UUID itemId) {
        Cart cart = getActiveCartDomain(customerId);
        cart.removeItem(itemId);

        Cart saved = cartRepository.save(cart);
        log.info("Removed item {} from cart {} for customer {}", itemId, saved.getId(), customerId);
        return CartDto.fromDomain(saved);
    }

    @Transactional
    public CartDto clearCart(String customerId) {
        Cart cart = getActiveCartDomain(customerId);
        cart.clear();

        Cart saved = cartRepository.save(cart);
        log.info("Cleared all items from cart {} for customer {}", saved.getId(), customerId);
        return CartDto.fromDomain(saved);
    }

    private Cart getActiveCartDomain(String customerId) {
        validateCustomerId(customerId);
        return cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                .orElseThrow(() -> new CartNotFoundException(customerId));
    }

    private Cart getOrCreateActiveCartDomain(String customerId) {
        validateCustomerId(customerId);
        return cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                .orElseGet(() -> {
                    try {
                        Cart newCart = Cart.createNewActiveCart(customerId, "INR");
                        return cartRepository.save(newCart);
                    } catch (DataIntegrityViolationException e) {
                        log.warn("Concurrent active cart creation detected for customer {}, retrying query...", customerId);
                        return cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                                .orElseThrow(() -> e);
                    }
                });
    }

    private void validateCustomerId(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer identity cannot be null or blank");
        }
    }
}
