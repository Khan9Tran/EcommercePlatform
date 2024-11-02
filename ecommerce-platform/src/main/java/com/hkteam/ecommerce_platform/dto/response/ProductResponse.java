package com.hkteam.ecommerce_platform.dto.response;

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
public class ProductResponse {
    String id;
    String slug;
    String name;
    String mainImageUrl;
    String originalPrice;
    String salePrice;
    String quantity;
}
