package com.hkteam.ecommerce_platform.dto.response;

import java.time.Instant;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BrandGetAllResponse {
    Long id;
    String logoUrl;
    String name;
    String description;
    Instant createdAt;
}
