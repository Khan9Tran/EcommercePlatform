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
public class CategoryResponse {
    Long id;
    String name;
    String description;
    String imageUrl;
    String iconUrl;
    String slug;
    Long parentId;
    String parentName;
    Instant createdAt;
    Instant lastUpdatedAt;
    List<ComponentResponse> listComponent;
}
