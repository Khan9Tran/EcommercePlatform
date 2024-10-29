package com.hkteam.ecommerce_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hkteam.ecommerce_platform.entity.product.Variant;

@Repository
public interface VariantRepository extends JpaRepository<Variant, String> {}
