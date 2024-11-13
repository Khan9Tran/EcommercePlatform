package com.hkteam.ecommerce_platform.repository;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hkteam.ecommerce_platform.entity.status.OrderStatus;

@Repository
public interface OrderStatusRepository extends JpaRepository<OrderStatus, String> {
    @NotNull
    Optional<OrderStatus> findByName(@NotNull String name);
}
