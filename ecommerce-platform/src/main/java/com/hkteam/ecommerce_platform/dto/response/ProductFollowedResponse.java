package com.hkteam.ecommerce_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductFollowedResponse {
    String productName;
    String slug;
    String mainImageUrl;
    String productId;
    String brandName;
    String logoBrand;
    String storeId;
    String storeName;
    String storeImageUrl;
}
