package com.hkteam.ecommerce_platform.dto.request;

import jakarta.validation.constraints.Size;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class BrandUpdateRequest {
    @Size(min = 2, max = 30, message = "BRAND_NAME_INVALID")
    String name;

    @Size(max = 255, message = "BRAND_DESCRIPTION_INVALID")
    String description;
}
