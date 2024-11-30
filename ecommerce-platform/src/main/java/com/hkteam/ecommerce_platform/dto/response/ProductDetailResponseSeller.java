package com.hkteam.ecommerce_platform.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ProductDetailResponseSeller {
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
}
