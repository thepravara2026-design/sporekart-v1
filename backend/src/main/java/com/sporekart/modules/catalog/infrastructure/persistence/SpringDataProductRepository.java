package com.sporekart.modules.catalog.infrastructure.persistence;

import com.sporekart.modules.catalog.domain.product.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataProductRepository extends JpaRepository<ProductEntity, UUID> {

    @Query("SELECT DISTINCT p FROM ProductEntity p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.variants WHERE p.sku = :sku")
    Optional<ProductEntity> findBySku(@Param("sku") String sku);

    boolean existsBySku(String sku);

    long countByCategoryId(UUID categoryId);

    @Query("SELECT DISTINCT p FROM ProductEntity p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.variants WHERE p.growerId = :growerId")
    List<ProductEntity> findAllByGrowerId(@Param("growerId") String growerId);

    @Query("SELECT DISTINCT p FROM ProductEntity p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.variants WHERE p.id = :id AND p.growerId = :growerId")
    Optional<ProductEntity> findByIdAndGrowerId(@Param("id") UUID id, @Param("growerId") String growerId);

    @Query(value = "SELECT DISTINCT p FROM ProductEntity p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.variants WHERE " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))",
           countQuery = "SELECT COUNT(p) FROM ProductEntity p WHERE " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<ProductEntity> findByFilters(@Param("search") String search,
                                      @Param("categoryId") UUID categoryId,
                                      @Param("status") ProductStatus status,
                                      @Param("minPrice") BigDecimal minPrice,
                                      @Param("maxPrice") BigDecimal maxPrice,
                                      Pageable pageable);

    /**
     * Deletes all images of a product. Called before a save that replaces the
     * image set so the unique (product_id, display_order) constraint is not
     * violated by insert-before-orphan-removal flush ordering.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ProductImageEntity img WHERE img.product.id = :productId")
    void deleteImagesByProductId(@Param("productId") UUID productId);
}
