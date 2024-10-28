package com.hkteam.ecommerce_platform.dto.response;

import com.hkteam.ecommerce_platform.entity.product.Variant;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductCreationResponse {
    String id;
    String slug;
    List<VariantOfProductResponse> variants;
}
