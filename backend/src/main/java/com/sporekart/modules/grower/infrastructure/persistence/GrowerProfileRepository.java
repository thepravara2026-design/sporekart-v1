package com.sporekart.modules.grower.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GrowerProfileRepository extends JpaRepository<GrowerProfileEntity, String> {
    Optional<GrowerProfileEntity> findByUserId(String userId);
}
