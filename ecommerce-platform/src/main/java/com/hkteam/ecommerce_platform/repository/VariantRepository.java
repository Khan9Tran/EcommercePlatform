package com.hkteam.ecommerce_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hkteam.ecommerce_platform.entity.product.Variant;

@Repository
public interface VariantRepository extends JpaRepository<Variant, String> {
    @Transactional
    @Modifying
    @Query("update Variant v set v.quantity = ?1 where v.id = ?2")
    void updateQuantityById(int quantity, String variantId);
}
