package com.hkteam.ecommerce_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductUserViewResponse {
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
    Set<VariantOfProductUserViewResponse> variants;
    List<AttributeOfProductResponse> attributes;

    Set<ImageInList> images;
    BrandOfProductResponse brand;
    CategoryOfProductResponse category;
    StoreOfProductResponse store;
    private Instant createdAt;
    private Instant lastUpdatedAt;
    boolean isBlocked;
    Set<ProductComponentValueOfProductResponse> components;
}
