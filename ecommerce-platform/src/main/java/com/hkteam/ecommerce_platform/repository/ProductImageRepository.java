package com.hkteam.ecommerce_platform.repository;

import com.hkteam.ecommerce_platform.entity.image.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
}
