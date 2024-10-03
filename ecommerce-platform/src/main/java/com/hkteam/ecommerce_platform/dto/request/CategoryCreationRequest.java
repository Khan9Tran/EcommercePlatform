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
public class CategoryCreationRequest {
    @ValidSpace
    String name;

    @ValidSpace
    String description;

    Long parentId;
}
