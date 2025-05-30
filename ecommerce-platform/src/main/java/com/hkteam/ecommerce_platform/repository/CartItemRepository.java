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

    @Query("""
			select c from CartItem c
			where c.product.id = ?1 and c.cart.user = ?2 and c.isCheckout = ?3""")
    Optional<CartItem> findByProductIdAndCartUserAndCartCartItemsAndIsCheckout(
            String id, User user, boolean isCheckout);

    @Query("""
			select c from CartItem c
			where c.variant.id = ?1 and c.cart.user = ?2 and c.isCheckout = ?3""")
    Optional<CartItem> findByVariantIdAndCartUserAndCartCartItemsAndIsCheckout(
            String id, User user, boolean isCheckout);

    @Query(
            "select c from CartItem c where c.cart.user = ?1 and c.isCheckout = false ORDER BY c.cart.lastUpdatedAt DESC")
    List<CartItem> findByCart_UserAndIsCheckoutFalse(User user, Pageable pageable);
}
