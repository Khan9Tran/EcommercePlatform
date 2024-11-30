package com.hkteam.ecommerce_platform.repository;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hkteam.ecommerce_platform.entity.product.Product;
import com.hkteam.ecommerce_platform.entity.user.Store;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByNameIgnoreCase(String name);

    Optional<Product> findBySlug(String slug);

    @NotNull
    Page<Product> findAll(@NotNull Pageable pageable);

    @NotNull
    List<Product> findAllById(@NotNull Iterable<Long> ids);

    Integer countByStore(Store store);

    Optional<Product> findById(String id);

    Page<Product> findByIsAvailableAndIsBlockedAndStore_IdAndNameContainsIgnoreCase(
            @Nullable boolean isAvailable,
            boolean isBlocked,
            @Nullable String id,
            @Nullable String name,
            Pageable pageable);

    @Transactional
    @Modifying
    @Query("update Product p set p.videoUrl = ?1 where p.id = ?2")
    int updateVideoUrlById(String videoUrl, String id);

    @Transactional
    @Modifying
    @Query("update Product p set p.mainImageUrl = ?1 where p.id = ?2")
    int updateMainImageUrlById(String mainImageUrl, String id);

    @Query("SELECT p FROM Product p WHERE p.store.id = ?1 ORDER BY p.lastUpdatedAt DESC")
    List<Product> findByLastUpdatedAtAndStoreId(@NotNull String storeId, Pageable pageable);
}
