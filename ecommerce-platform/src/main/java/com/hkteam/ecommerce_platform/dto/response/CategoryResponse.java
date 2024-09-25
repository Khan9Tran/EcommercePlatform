package com.hkteam.ecommerce_platform.dto.response;

import java.time.Instant;
import java.util.Set;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryResponse {
    Long id;
    String name;
    String description;
    String imageUrl;
    String iconUrl;
    String slug;
    Long parentId;
    Instant createdAt;
    Instant lastUpdatedAt;
    Set<ComponentResponse> listComponent;
}
