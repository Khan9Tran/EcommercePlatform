package com.hkteam.ecommerce_platform.dto.response;

import java.time.Instant;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ComponentDetailResponse {
    Long id;
    Long categoryId;
    String name;
    List<ProductComponentValueResponse> values;
}
