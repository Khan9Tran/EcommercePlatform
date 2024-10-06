package com.hkteam.ecommerce_platform.repository;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hkteam.ecommerce_platform.entity.image.ProductImage;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    @NotNull
    List<ProductImage> findAllById(@NotNull Iterable<Long> ids);
}
