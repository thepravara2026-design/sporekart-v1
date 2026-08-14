package com.sporekart.modules.cart.infrastructure.persistence;

import com.sporekart.modules.cart.domain.Cart;
import com.sporekart.modules.cart.domain.CartStatus;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository {

    Cart save(Cart cart);

    Optional<Cart> findById(UUID id);

    Optional<Cart> findByCustomerIdAndStatus(String customerId, CartStatus status);

    void deleteById(UUID id);
}
