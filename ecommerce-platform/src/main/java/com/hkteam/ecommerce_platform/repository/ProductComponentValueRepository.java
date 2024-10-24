package com.hkteam.ecommerce_platform.repository;

import com.hkteam.ecommerce_platform.entity.category.ProductComponentValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductComponentValueRepository extends JpaRepository<ProductComponentValue, Long> {
}
