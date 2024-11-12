package com.hkteam.ecommerce_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemDetailResponse {
    String name;
    int quantity;
    BigDecimal originalPrice;
    BigDecimal salePrice;
    String image;
    List<String> value;
    String variantId;
    String productId;
    String productSlug;
    String variantSlug;
    boolean isAvailable;
}
