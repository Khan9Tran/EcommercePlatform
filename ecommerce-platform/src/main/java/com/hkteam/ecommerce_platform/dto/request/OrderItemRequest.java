package com.hkteam.ecommerce_platform.dto.request;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class OrderItemRequest {
    String productId;
    String variantId;
    Integer quantity;
    BigDecimal salePrice;
    BigDecimal originalPrice;
}
