package com.hkteam.ecommerce_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartResponse {
    String storeName;
    String storeSlug;
    List<CartItemDetailResponse> items;
}
