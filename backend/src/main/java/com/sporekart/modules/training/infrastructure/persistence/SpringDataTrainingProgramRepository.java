package com.sporekart.modules.training.infrastructure.persistence;

import com.sporekart.modules.training.domain.ProgramStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataTrainingProgramRepository extends JpaRepository<TrainingProgramEntity, String> {
    Optional<TrainingProgramEntity> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsByTitleIgnoreCase(String title);
    Page<TrainingProgramEntity> findByStatus(ProgramStatus status, Pageable pageable);

    @Query("SELECT p FROM TrainingProgramEntity p WHERE " +
           "(:search IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:category IS NULL OR UPPER(p.category) = UPPER(:category))")
    Page<TrainingProgramEntity> searchPrograms(@Param("search") String search,
                                                @Param("status") ProgramStatus status,
                                                @Param("category") String category,
                                                Pageable pageable);
}
