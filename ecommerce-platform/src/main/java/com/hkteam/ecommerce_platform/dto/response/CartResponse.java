package com.hkteam.ecommerce_platform.dto.response;

import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartResponse {
    String storeName;
    String storeId;
    String storeSlug;
    String avatarStore;
    Float ratingStore;
    List<CartItemDetailResponse> items;
}
