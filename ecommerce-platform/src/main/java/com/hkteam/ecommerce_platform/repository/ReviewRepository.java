package com.hkteam.ecommerce_platform.repository;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hkteam.ecommerce_platform.entity.useractions.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @NotNull
    Optional<Review> findById(@NotNull Long id);

    boolean existsById(@NotNull Long id);
}
