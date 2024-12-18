package com.hkteam.ecommerce_platform.repository;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import com.hkteam.ecommerce_platform.entity.category.Component;

@Repository
public interface ComponentRepository extends JpaRepository<Component, Long> {
    boolean existsByNameIgnoreCase(String name);

    @NotNull
    Optional<Component> findById(@NotNull Long id);

    @NotNull
    Page<Component> findAll(@NotNull Pageable pageable);

    List<Component> findByCategoriesId(Long id);

    Page<Component> findByNameContainsIgnoreCase(@Nullable String name, Pageable pageable);

}
