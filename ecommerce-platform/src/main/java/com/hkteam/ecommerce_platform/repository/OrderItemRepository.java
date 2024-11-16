package com.hkteam.ecommerce_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hkteam.ecommerce_platform.entity.order.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {}
