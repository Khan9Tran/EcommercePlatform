package com.hkteam.ecommerce_platform.repository;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hkteam.ecommerce_platform.entity.product.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByNameIgnoreCase(String name);

    Optional<Product> findBySlug(String slug);

    @NotNull
    Page<Product> findAll(@NotNull Pageable pageable);

    @NotNull
    List<Product> findAllById(@NotNull Iterable<Long> ids);
}
