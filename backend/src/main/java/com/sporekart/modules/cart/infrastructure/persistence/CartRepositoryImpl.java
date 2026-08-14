package com.sporekart.modules.cart.infrastructure.persistence;

import com.sporekart.modules.cart.domain.Cart;
import com.sporekart.modules.cart.domain.CartItem;
import com.sporekart.modules.cart.domain.CartStatus;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class CartRepositoryImpl implements CartRepository {

    private final SpringDataJpaCartRepository jpaRepository;

    public CartRepositoryImpl(SpringDataJpaCartRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Cart save(Cart cart) {
        CartEntity entityToSave;

        if (cart.getVersion() != null) {
            Optional<CartEntity> existingOpt = jpaRepository.findByIdWithItems(cart.getId());
            if (existingOpt.isPresent()) {
                entityToSave = existingOpt.get();
                entityToSave.setStatus(cart.getStatus());
                entityToSave.setCurrency(cart.getCurrency());
                entityToSave.setSubtotal(cart.getSubtotal());
                entityToSave.setItemCount(cart.getItemCount());
                entityToSave.setUpdatedAt(cart.getUpdatedAt());

                // Remove item entities no longer present in domain aggregate
                entityToSave.getItems().removeIf(existingItem ->
                        cart.getItems().stream().noneMatch(domainItem -> domainItem.getId().equals(existingItem.getId()))
                );

                // Update existing item entities or add new item entities
                for (CartItem domainItem : cart.getItems()) {
                    Optional<CartItemEntity> existingItemOpt = entityToSave.getItems().stream()
                            .filter(item -> item.getId().equals(domainItem.getId()))
                            .findFirst();

                    if (existingItemOpt.isPresent()) {
                        CartItemEntity existingItem = existingItemOpt.get();
                        existingItem.setQuantity(domainItem.getQuantity());
                        existingItem.setUnitPriceSnapshot(domainItem.getUnitPriceSnapshot());
                        existingItem.setLineTotal(domainItem.getLineTotal());
                        existingItem.setUpdatedAt(domainItem.getUpdatedAt());
                    } else {
                        CartItemEntity newItemEntity = CartItemEntity.fromDomain(domainItem, entityToSave);
                        entityToSave.getItems().add(newItemEntity);
                    }
                }
            } else {
                entityToSave = CartEntity.fromDomain(cart);
            }
        } else {
            entityToSave = CartEntity.fromDomain(cart);
        }

        CartEntity savedEntity = jpaRepository.save(entityToSave);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Cart> findById(UUID id) {
        return jpaRepository.findByIdWithItems(id)
                .map(CartEntity::toDomain);
    }

    @Override
    public Optional<Cart> findByCustomerIdAndStatus(String customerId, CartStatus status) {
        return jpaRepository.findByCustomerIdAndStatusWithItems(customerId, status)
                .map(CartEntity::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
