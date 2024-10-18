package com.hkteam.ecommerce_platform.repository;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hkteam.ecommerce_platform.entity.user.Store;

@Repository
public interface StoreRepository extends JpaRepository<Store, String> {
    @NotNull
    Optional<Store> findById(@NotNull String id);

    @NotNull
    Page<Store> findAll(@NotNull Pageable pageable);
}
