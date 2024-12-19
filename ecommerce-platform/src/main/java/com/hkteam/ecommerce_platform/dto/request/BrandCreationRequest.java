package com.hkteam.ecommerce_platform.dto.request;

import jakarta.validation.constraints.Size;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class BrandCreationRequest {
    @Size(min = 2, max = 30, message = "MIN_MAX_INVALID")
    String name;

    @Size(max = 255, message = "MAX_INVALID")
    String description;
}
