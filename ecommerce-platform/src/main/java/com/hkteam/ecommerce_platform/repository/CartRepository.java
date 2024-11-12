package com.hkteam.ecommerce_platform.repository;

import com.hkteam.ecommerce_platform.entity.cart.Cart;
import com.hkteam.ecommerce_platform.entity.user.Store;
import com.hkteam.ecommerce_platform.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserAndStore(User user, Store store);

    @Query("select c from Cart c where c.user = ?1 and c.isAvailable = ?2")
    Page<Cart> findByUserAndIsAvailable(User user, boolean isAvailable, Pageable pageable);



}