package com.hkteam.ecommerce_platform.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemDetailResponse {
    Long id;
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
    String brand;
    String logoBrand;
}
