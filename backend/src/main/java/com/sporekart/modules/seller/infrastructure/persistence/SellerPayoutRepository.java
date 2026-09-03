package com.sporekart.modules.seller.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SellerPayoutRepository extends JpaRepository<SellerPayoutEntity, UUID> {
    List<SellerPayoutEntity> findAllBySellerId(String sellerId);
}
