package com.sporekart.modules.returns.infrastructure.persistence;

import com.sporekart.modules.returns.domain.Return;
import com.sporekart.modules.returns.domain.ReturnRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ReturnRepositoryImpl implements ReturnRepository {

    private final SpringDataJpaReturnRepository jpaRepository;

    public ReturnRepositoryImpl(SpringDataJpaReturnRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Return save(Return returnAggregate) {
        Optional<ReturnEntity> existingOpt = jpaRepository.findById(returnAggregate.getId());
        ReturnEntity entity;
        if (existingOpt.isPresent()) {
            entity = existingOpt.get();
            entity.updateFromDomain(returnAggregate);
        } else {
            entity = ReturnEntity.fromDomain(returnAggregate);
        }
        ReturnEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Return> findById(UUID id) {
        return jpaRepository.findById(id).map(ReturnEntity::toDomain);
    }

    @Override
    public Optional<Return> findByReturnReference(String returnReference) {
        return jpaRepository.findByReturnReference(returnReference).map(ReturnEntity::toDomain);
    }

    @Override
    public List<Return> findByOrderId(UUID orderId) {
        return jpaRepository.findByOrderId(orderId).stream()
                .map(ReturnEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Return> findByCustomerId(String customerId) {
        return jpaRepository.findByCustomerId(customerId).stream()
                .map(ReturnEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Return> findByOrderReference(String orderReference) {
        return jpaRepository.findByOrderReference(orderReference).stream()
                .map(ReturnEntity::toDomain)
                .collect(Collectors.toList());
    }
}
