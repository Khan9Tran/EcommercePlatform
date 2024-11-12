package com.hkteam.ecommerce_platform.repository;

import com.hkteam.ecommerce_platform.entity.cart.Cart;
import com.hkteam.ecommerce_platform.entity.cart.CartItem;
import com.hkteam.ecommerce_platform.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByProductIdAndCartUser(String id, User user);
}
