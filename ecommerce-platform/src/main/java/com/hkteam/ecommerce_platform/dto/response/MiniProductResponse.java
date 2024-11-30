package com.hkteam.ecommerce_platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class MiniProductResponse {
    String id;
    String name;
    String slug;
    String mainImageUrl;
    BigDecimal originalPrice;
    BigDecimal salePrice;
    Float rating;
    String brandName;
}
