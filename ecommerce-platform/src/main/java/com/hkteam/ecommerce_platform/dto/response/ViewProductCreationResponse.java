package com.hkteam.ecommerce_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ViewProductCreationResponse {
    String productId;
    String userId;
    int count;
}
