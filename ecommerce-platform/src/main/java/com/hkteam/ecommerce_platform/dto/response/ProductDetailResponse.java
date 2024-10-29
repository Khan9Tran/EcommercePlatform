package com.hkteam.ecommerce_platform.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

import jakarta.persistence.*;

import com.hkteam.ecommerce_platform.entity.image.ProductImage;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductDetailResponse {
    String id;
    String slug;
    String name;
    String description;
    String details;
    String videoUrl;
    String mainImageUrl;
    BigDecimal originalPrice;
    BigDecimal salePrice;
    boolean isAvailable;
    int quantity;
    Float rating;

    Set<ProductImage> images;
    BrandOfProductResponse brand;
    CategoryOfProductResponse category;
    StoreOfProductResponse store;
    private Instant createdAt;
    private Instant lastUpdatedAt;
    boolean isBlocked;
    Set<ProductComponentValueOfProductResponse> components;
}
