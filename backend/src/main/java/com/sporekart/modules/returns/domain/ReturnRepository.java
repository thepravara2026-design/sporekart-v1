package com.sporekart.modules.returns.domain;

// ReturnRepository Port in domain package
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReturnRepository {
    Return save(Return returnAggregate);
    Optional<Return> findById(UUID id);
    Optional<Return> findByReturnReference(String returnReference);
    List<Return> findByOrderId(UUID orderId);
    List<Return> findByCustomerId(String customerId);
    List<Return> findByOrderReference(String orderReference);
}
