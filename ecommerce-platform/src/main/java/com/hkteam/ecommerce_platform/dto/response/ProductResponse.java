package com.hkteam.ecommerce_platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ProductResponse {
    String id;
    String slug;
    String name;
    String mainImageUrl;
    String videoUrl;
    String originalPrice;
    String salePrice;
    String quantity;
    Instant createdAt;
    Instant lastUpdatedAt;
    Float rating;
}
