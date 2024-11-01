package com.hkteam.ecommerce_platform.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class CategoryUpdateRequest {
    @NotBlank(message = "NOT_BLANK")
    @NonNull
    String name;

    @NonNull
    String description;

    Long parentId;
}
