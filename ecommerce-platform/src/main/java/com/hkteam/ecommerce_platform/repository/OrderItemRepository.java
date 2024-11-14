package com.hkteam.ecommerce_platform.repository;

import com.hkteam.ecommerce_platform.entity.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
