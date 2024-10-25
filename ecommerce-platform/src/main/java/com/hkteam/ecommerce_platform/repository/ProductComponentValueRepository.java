package com.hkteam.ecommerce_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hkteam.ecommerce_platform.entity.category.ProductComponentValue;

@Repository
public interface ProductComponentValueRepository extends JpaRepository<ProductComponentValue, Long> {}
