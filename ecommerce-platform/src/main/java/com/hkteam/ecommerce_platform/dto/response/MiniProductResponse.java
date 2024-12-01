package com.hkteam.ecommerce_platform.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

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
