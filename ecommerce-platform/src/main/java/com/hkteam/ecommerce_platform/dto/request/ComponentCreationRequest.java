package com.hkteam.ecommerce_platform.dto.request;

import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ComponentCreationRequest {
    @Size(min = 3, max = 100, message = "MIN_MAX_INVALID")
    String name;

    boolean required;
}
