package com.hkteam.ecommerce_platform.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CustomerResponse {
    String id;
    String username;
    String name;
    String createdAt;
    Boolean isBlocked;
    String imageUrl;
}
