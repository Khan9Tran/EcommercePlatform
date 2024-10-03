package com.hkteam.ecommerce_platform.dto.request;

import com.hkteam.ecommerce_platform.validator.ValidSpace;

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
public class ComponentUpdateRequest {
    @ValidSpace
    String name;

    boolean required;
}
