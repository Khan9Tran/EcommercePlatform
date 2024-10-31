package com.hkteam.ecommerce_platform.repository;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hkteam.ecommerce_platform.entity.user.Store;

@Repository
public interface StoreRepository extends JpaRepository<Store, String> {
    @NotNull
    Optional<Store> findByUserId(@NotNull String userId);

    @NotNull
    Page<Store> findAll(@NotNull Pageable pageable);

    @Query(
            "select s from Store s where lower(s.name) like lower(concat('%', ?1, '%')) or lower(s.user.username) like lower(concat('%', ?2, '%'))")
    Page<Store> searchAllStore(String name, String username, Pageable pageable);
}
