package com.hkteam.ecommerce_platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hkteam.ecommerce_platform.entity.cart.CartItem;
import com.hkteam.ecommerce_platform.entity.user.User;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByProductIdAndCartUser(String id, User user);
    Optional<CartItem> findByVariantIdAndCartUser(String id, User user);

    @Query("SELECT e FROM CartItem e WHERE e.cart.user = ?1 ORDER BY e.cart.lastUpdatedAt DESC")
    List<CartItem> findByUpdatedAtAndUser(User user, Pageable pageable);



}
