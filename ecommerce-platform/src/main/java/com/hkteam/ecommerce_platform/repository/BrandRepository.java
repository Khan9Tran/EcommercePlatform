package com.hkteam.ecommerce_platform.repository;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hkteam.ecommerce_platform.entity.product.Brand;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    boolean existsByNameIgnoreCase(String name);

    @NotNull
    Optional<Brand> findById(@NotNull Long id);

    @NotNull
    Page<Brand> findAll(@NotNull Pageable pageable);
}
