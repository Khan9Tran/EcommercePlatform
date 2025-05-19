package com.hkteam.ecommerce_platform.repository;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hkteam.ecommerce_platform.entity.product.Product;
import com.hkteam.ecommerce_platform.entity.user.Store;
import com.hkteam.ecommerce_platform.entity.user.User;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    Optional<Product> findBySlug(String slug);

    @NotNull
    Page<Product> findAll(@NotNull Pageable pageable);

    @NotNull
    List<Product> findAllById(@NotNull Iterable<String> ids);

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

    List<Product> findByFollowersAndIsAvailableTrueAndIsBlockedFalse(User followers);

    boolean existsById(@NotNull String productId);

    @Query(
            """
				SELECT p FROM Product p
				WHERE p.isAvailable = true AND p.isBlocked = false
				ORDER BY p.sold DESC, p.rating DESC, p.salePrice, p.createdAt ASC
				LIMIT :productLimit
			""")
    List<Product> findProductBestSelling(@Param("productLimit") int productLimit);

    @Query(
            """
				SELECT p FROM Product p
				WHERE p.isAvailable = true AND p.isBlocked = false
				ORDER BY
					(COALESCE(SIZE(p.orderItems), 0) * 4 +
					COALESCE(SIZE(p.followers), 0) * 3 +
					COALESCE(SIZE(p.cartItems), 0) * 2 +
					COALESCE(SIZE(p.reviews), 0) * 1 +
					COALESCE(SIZE(p.viewProducts), 0) * 1) DESC
				LIMIT :productLimit
			""")
    List<Product> findProductBestInteraction(@Param("productLimit") int productLimit);
}
