package com.hkteam.ecommerce_platform.repository;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hkteam.ecommerce_platform.entity.useractions.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @NotNull
    Optional<Review> findById(@NotNull Long id);

    boolean existsById(@NotNull Long id);

    @Query("SELECT COUNT(r) > 0 FROM Review r JOIN r.products p WHERE r.user.id = :userId AND p.id = :productId")
    boolean hasUserAlreadyReviewedProduct(String userId, String productId);

    @Query("select r from Review r join r.products p "
            + "where (p.id = :productId and p.isBlocked = false and p.isAvailable = true) ")
    List<Review> findAllReviewByProductId(String productId);

    @Query("select r from Review r join r.products p "
            + "where (p.id = :productId and p.isBlocked = false and p.isAvailable = true) "
            + "and (:starNumber = '' or r.rating = cast(:starNumber as float)) "
            + "and (:commentString = 'commentString' and (r.comment is not null and r.comment != '') "
            + "     or (:commentString != 'commentString' and (r.comment = :commentString or :commentString = ''))) "
            + "and (:mediaString = '' or (r.videoUrl is not null and r.videoUrl != '') or (r.images is not empty))")
    Page<Review> findAllReviewProductId(
            String productId,
            @Nullable String starNumber,
            @Nullable String commentString,
            @Nullable String mediaString,
            Pageable pageable);

    @Query("select r.rating, count (r) from Review r join r.products p where p.id = :productId group by r.rating")
    List<Object[]> countReviewsByRating(String productId);
}
