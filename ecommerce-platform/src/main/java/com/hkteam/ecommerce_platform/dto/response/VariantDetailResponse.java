package com.hkteam.ecommerce_platform.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VariantDetailResponse {
    String id;
    String slug;
    BigDecimal originalPrice;
    BigDecimal salePrice;
    boolean isAvailable;
    int quantity;
    List<ValueOfVariantResponse> values;
    String imageUrl;
    ProductOfVariantResponse product;
    private Instant createdAt;
    private Instant lastUpdatedAt;
}
