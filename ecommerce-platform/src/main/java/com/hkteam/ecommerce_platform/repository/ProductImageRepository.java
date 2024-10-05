package com.hkteam.ecommerce_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hkteam.ecommerce_platform.entity.image.ProductImage;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {}
