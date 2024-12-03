package com.hkteam.ecommerce_platform.repository;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hkteam.ecommerce_platform.entity.useractions.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @NotNull
    Optional<Review> findById(@NotNull Long id);

    boolean existsById(@NotNull Long id);

    @Query(
            "SELECT count(r) > 0 FROM Review r JOIN r.products p JOIN p.orderItems oi WHERE r.user.id = :userId AND oi.order.id = :orderId")
    boolean hasUserAlreadyReviewedOrder(String userId, String orderId);
}
