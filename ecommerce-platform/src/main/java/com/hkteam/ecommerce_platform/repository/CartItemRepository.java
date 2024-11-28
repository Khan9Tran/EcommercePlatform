package com.hkteam.ecommerce_platform.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hkteam.ecommerce_platform.entity.cart.CartItem;
import com.hkteam.ecommerce_platform.entity.user.User;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByProductIdAndCartUser(String id, User user);
    Optional<CartItem> findByVariantIdAndCartUser(String id, User user);
}
