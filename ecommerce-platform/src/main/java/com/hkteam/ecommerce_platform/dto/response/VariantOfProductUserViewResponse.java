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
public class VariantOfProductUserViewResponse {
    String id;
    String slug;
    BigDecimal originalPrice;
    BigDecimal salePrice;
    int quantity;
    List<ValueOfVariantResponse> values;
}
