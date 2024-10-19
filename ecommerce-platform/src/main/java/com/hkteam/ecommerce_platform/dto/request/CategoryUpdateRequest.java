package com.hkteam.ecommerce_platform.dto.request;

import com.hkteam.ecommerce_platform.validator.ValidSpace;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class CategoryUpdateRequest {
    @NonNull
    @ValidSpace
    String name;

    @ValidSpace
    String description;

    Long parentId;
}
