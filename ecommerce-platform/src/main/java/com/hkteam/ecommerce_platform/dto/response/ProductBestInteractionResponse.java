package com.hkteam.ecommerce_platform.dto.response;

import java.math.BigDecimal;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductBestInteractionResponse {
    String name;
    String videoUrl;
    String mainImageUrl;
    BigDecimal salePrice;
    int sold;
    int percentDiscount;
    String slug;
}
